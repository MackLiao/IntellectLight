package com.tongji.relation.api;

import com.tongji.relation.service.RelationService;
import com.tongji.auth.token.JwtService;
import com.tongji.profile.api.dto.ProfileResponse;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.nio.charset.StandardCharsets;

/**
 * Relation API controller.
 * Responsibilities: follow/unfollow, three-state relation query, following/follower lists (offset and cursor pagination), user-dimension counter reads and sampling self-checks.
 * Caching: ZSet stores following/follower lists; user counters use a fixed SDS structure (5 x 4 bytes, big-endian encoding), with sampling consistency checks and on-demand rebuilds.
 */
@RestController
@RequestMapping("/api/v1/relation")
public class RelationController {
    private final RelationService relationService;
    private final JwtService jwtService;
    private final StringRedisTemplate redis;
    private final com.tongji.counter.service.UserCounterService userCounterService;
    private final com.tongji.relation.mapper.RelationMapper relationMapper;

    public RelationController(RelationService relationService, JwtService jwtService, StringRedisTemplate redis, com.tongji.counter.service.UserCounterService userCounterService, com.tongji.relation.mapper.RelationMapper relationMapper) {
        this.relationService = relationService;
        this.jwtService = jwtService;
        this.redis = redis;
        this.userCounterService = userCounterService;
        this.relationMapper = relationMapper;
    }

    /**
     * Follow a user.
     * @param toUserId the user ID to follow
     * @param jwt authentication token
     * @return whether the follow was successful
     */
    @PostMapping("/follow")
    public boolean follow(@RequestParam("toUserId") long toUserId, @AuthenticationPrincipal Jwt jwt) {
        long uid = jwtService.extractUserId(jwt);
        return relationService.follow(uid, toUserId);
    }

    /**
     * Unfollow a user.
     * @param toUserId the user ID to unfollow
     * @param jwt authentication token
     * @return whether the unfollow was successful
     */
    @PostMapping("/unfollow")
    public boolean unfollow(@RequestParam("toUserId") long toUserId, @AuthenticationPrincipal Jwt jwt) {
        long uid = jwtService.extractUserId(jwt);
        return relationService.unfollow(uid, toUserId);
    }

    /**
     * Query the three-state relation with a target user.
     * @param toUserId target user ID
     * @param jwt authentication token
     * @return three-state result: following/followedBy/mutual
     */
    @GetMapping("/status")
    public Map<String, Boolean> status(@RequestParam("toUserId") long toUserId, @AuthenticationPrincipal Jwt jwt) {
        long uid = jwtService.extractUserId(jwt);
        return relationService.relationStatus(uid, toUserId);
    }

    /**
     * Get the following list with offset or cursor pagination.
     * @param userId user ID
     * @param limit maximum number of results to return
     * @param offset offset (effective when cursor is null)
     * @param cursor cursor (millisecond timestamp)
     * @return list of followed user IDs
     */
    @GetMapping("/following")
    public List<ProfileResponse> following(@RequestParam("userId") long userId,
                                @RequestParam(value = "limit", defaultValue = "20") int limit,
                                @RequestParam(value = "offset", defaultValue = "0") int offset,
                                @RequestParam(value = "cursor", required = false) Long cursor) {
        int l = Math.min(Math.max(limit, 1), 100);
        return relationService.followingProfiles(userId, l, Math.max(offset, 0), cursor);
    }

    /**
     * Get the follower list with offset or cursor pagination.
     * @param userId user ID
     * @param limit maximum number of results to return
     * @param offset offset (effective when cursor is null)
     * @param cursor cursor (millisecond timestamp)
     * @return list of follower user IDs
     */
    @GetMapping("/followers")
    public List<ProfileResponse> followers(@RequestParam("userId") long userId,
                                          @RequestParam(value = "limit", defaultValue = "20") int limit,
                                          @RequestParam(value = "offset", defaultValue = "0") int offset,
                                          @RequestParam(value = "cursor", required = false) Long cursor) {
        int l = Math.min(Math.max(limit, 1), 100);
        return relationService.followersProfiles(userId, l, Math.max(offset, 0), cursor);
    }

    /**
     * Get user-dimension counters (SDS).
     * Structure and consistency: SDS consists of 5 x 4-byte segments (followings/followers/posts/likes received/favorites received); sampling checks and rebuilds are triggered on demand to ensure API stability.
     * @param userId user ID
     * @return counter metric values
     */
    @GetMapping("/counter")
    public Map<String, Long> counter(@RequestParam("userId") long userId) {
        // Read user counter string from Redis (SDS, key: ucnt:{userId})
        byte[] raw = redis.execute((RedisCallback<byte[]>)
                c -> c.stringCommands().get(("ucnt:" + userId).getBytes(StandardCharsets.UTF_8)));

        // Build counter result
        Map<String, Long> m = new LinkedHashMap<>();

        // Attempt rebuild when missing or structurally invalid (fewer than 5 segments x 4 bytes each)
        if (raw == null || raw.length < 20) {
            try {
                userCounterService.rebuildAllCounters(userId);
            } catch (Exception ignored) {}

            // Second read after rebuild
            raw = redis.execute((RedisCallback<byte[]>)
                    c -> c.stringCommands().get(("ucnt:" + userId).getBytes(StandardCharsets.UTF_8)));

            // If still failed, return 0 to ensure API availability
            if (raw == null || raw.length < 20) {
                m.put("followings", 0L);
                m.put("followers", 0L);
                m.put("posts", 0L);
                m.put("likedPosts", 0L);
                m.put("favedPosts", 0L);
                return m;
            }
        }

        final byte[] buf = raw;
        // Number of segments (4 bytes each, big-endian 32-bit integer encoding)
        final int seg = buf.length / 4;

        // Read the counter at segment idx (1-based index), assembled as big-endian long
        IntFunction<Long> read = idx -> {
            if (idx < 1 || idx > seg) return 0L;
            int off = (idx - 1) * 4;
            long n = 0;
            for (int i = 0; i < 4; i++) {
                n = (n << 8) | (buf[off + i] & 0xFFL);
            }
            return n;
        };

        long sdsFollowings = read.apply(1);
        long sdsFollowers = read.apply(2);

        String chkKey = "ucnt:chk:" + userId;
        // Sampling check: rate-limited with a Redis lock, triggered once per user every 300s
        Boolean doCheck = redis.opsForValue().setIfAbsent(chkKey, "1", java.time.Duration.ofSeconds(300));

        if (Boolean.TRUE.equals(doCheck)) {
            int dbFollowings = 0;
            int dbFollowers = 0;

            // Only verify active following/follower counts and compare with SDS values
            try {
                dbFollowings = relationMapper.countFollowingActive(userId);
            } catch (Exception ignored) {}
            try {
                dbFollowers = relationMapper.countFollowerActive(userId);
            } catch (Exception ignored) {}

            // Trigger full rebuild if segment count is invalid or values are inconsistent
            if ((seg != 5) || sdsFollowings != (long) dbFollowings || sdsFollowers != (long) dbFollowers) {
                try {
                    userCounterService.rebuildAllCounters(userId);
                } catch (Exception ignored) {}

                // Read after rebuild and return the latest values directly
                byte[] raw2 = redis.execute((RedisCallback<byte[]>)
                        c -> c.stringCommands().get(("ucnt:" + userId).getBytes(StandardCharsets.UTF_8)));
                if (raw2 != null && raw2.length >= 20) {
                    final byte[] buf2 = raw2;
                    // Second read function: also reads as big-endian 32-bit
                    IntFunction<Long> r2 = idx -> {
                        int off = (idx - 1) * 4;
                        long n = 0;
                        for (int i = 0; i < 4; i++) {
                            n = (n << 8) | (buf2[off + i] & 0xFFL);
                        }
                        return n;
                    };
                    m.put("followings", r2.apply(1));
                    m.put("followers", r2.apply(2));
                    m.put("posts", r2.apply(3));
                    m.put("likedPosts", r2.apply(4));
                    m.put("favedPosts", r2.apply(5));
                    return m;
                }
            }
        }

        // Normal path: return counter values directly from SDS
        m.put("followings", sdsFollowings);
        m.put("followers", sdsFollowers);
        m.put("posts", read.apply(3));
        m.put("likedPosts", read.apply(4));
        m.put("favedPosts", read.apply(5));
        return m;
    }
}

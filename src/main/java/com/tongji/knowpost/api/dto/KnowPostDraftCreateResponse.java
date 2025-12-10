package com.tongji.knowpost.api.dto;

/**
 * Draft creation response: returns the new post ID as a string to avoid frontend precision loss.
 */
public record KnowPostDraftCreateResponse(String id) {

}
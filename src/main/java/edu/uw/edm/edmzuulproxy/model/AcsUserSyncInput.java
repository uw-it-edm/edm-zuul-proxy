package edu.uw.edm.edmzuulproxy.model;


import com.google.common.base.Preconditions;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class AcsUserSyncInput {
    private final Map<String, String> headers = new HashMap<>();

    public AcsUserSyncInput(String authenticationHeaderName, String userName) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(authenticationHeaderName), "AuthenticationHeaderName is required");
        Preconditions.checkArgument(StringUtils.isNotEmpty(userName), "UserName is required.");

        headers.put(authenticationHeaderName, userName);
    }
}

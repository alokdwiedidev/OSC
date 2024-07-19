package com.osc.userservice.grpcclient;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import session.Session;
import session.SessionServiceGrpc;

@Service
public class LoginServiceClient {
    @GrpcClient("sessionService")
    private SessionServiceGrpc.SessionServiceBlockingStub blockingStub;

    public boolean getLoginStatus(String userId, String device) {
        Session.LoginCheckRequest loginCheckRequest = Session.LoginCheckRequest.newBuilder().
                setUserId(userId).setDevice(device).build();
        Session.LoginCheckResponse loginCheckResponse = blockingStub.isUserLoggedIn(loginCheckRequest);
        return loginCheckResponse.getIsLoggedIn();
    }

    public boolean isSessionIdValid(String userId, String sessionId) {
        Session.LogoutSessionIdCheckRequest sessionIdCheckRequest = Session.LogoutSessionIdCheckRequest.newBuilder()
                .setUserId(userId).setSessionId(sessionId)
                .build();

        Session.LogoutSessionIdCheckResponse sessionIdCheckResponse = blockingStub.sessionIdCheck(sessionIdCheckRequest);
        return !sessionIdCheckResponse.getIsSessionIdValid();
    }
}

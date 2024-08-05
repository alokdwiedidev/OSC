package com.osc.userservice.grpcclient;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import session.Session;
import session.SessionServiceGrpc;

@Service
public class LoginServiceClient {
    @GrpcClient("sessionService")
    private SessionServiceGrpc.SessionServiceBlockingStub blockingStub;

    public boolean getLoginStatus(String userKey) {
        Session.LoginCheckRequest loginCheckRequest = Session.LoginCheckRequest.newBuilder().
               setUserKey(userKey).build();
        Session.LoginCheckResponse loginCheckResponse = blockingStub.isUserLoggedIn(loginCheckRequest);
        return loginCheckResponse.getIsLoggedIn();
    }

    public boolean isSessionIdValid(String userKey, String sessionId) {
        Session.LogoutSessionIdCheckRequest sessionIdCheckRequest = Session.LogoutSessionIdCheckRequest.newBuilder()
                .setUserKey(userKey).setSessionId(sessionId)
                .build();

        Session.LogoutSessionIdCheckResponse sessionIdCheckResponse = blockingStub.sessionIdCheck(sessionIdCheckRequest);
        return sessionIdCheckResponse.getIsSessionIdValid();
    }
}

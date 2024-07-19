package com.osc.sessionservice.service;


import com.osc.sessionservice.entity.SessionEntity;
import com.osc.sessionservice.repository.SessionRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import session.Session;
import session.SessionServiceGrpc;

import java.util.Optional;


@GrpcService
public class GrpcServiceImpl extends SessionServiceGrpc.SessionServiceImplBase {
    @Autowired
    private SessionRepository sessionRepository;

    @Override
    public void isUserLoggedIn(Session.LoginCheckRequest request, StreamObserver<Session.LoginCheckResponse> responseObserver) {
        String userId = request.getUserId();
        String device = request.getDevice();

        // Fetch the current session information from the database
        Optional<SessionEntity> currentSession = Optional.ofNullable(sessionRepository.findByUserIdAndLogoutTimeIsNull(userId));

        Session.LoginCheckResponse response;
        if (currentSession.isPresent()) {
            if (device.equals(currentSession.get().getDevice())) {
                // If the same device, the user is considered logged in
                response = Session.LoginCheckResponse.newBuilder()
                        .setIsLoggedIn(true)
                        .build();
            } else {
                // If a different device, consider the user not logged in
                response = Session.LoginCheckResponse.newBuilder()
                        .setIsLoggedIn(false)
                        .build();
            }
        } else {
            // If no current session, the user is considered not logged in
            response = Session.LoginCheckResponse.newBuilder()
                    .setIsLoggedIn(false)
                    .build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void sessionIdCheck(Session.LogoutSessionIdCheckRequest request, StreamObserver<Session.LogoutSessionIdCheckResponse> responseObserver) {
        String userId = request.getUserId();
        String sessionId = request.getSessionId();
        SessionEntity session = sessionRepository.findByUserId(userId);
        if (session != null && session.getSessionId().equals(sessionId)) {
            // Session ID matches, send response
            Session.LogoutSessionIdCheckResponse response = Session.LogoutSessionIdCheckResponse.newBuilder()
                    .setIsSessionIdValid(true)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            // Session ID does not match or session not found
            Session.LogoutSessionIdCheckResponse response = Session.LogoutSessionIdCheckResponse.newBuilder()
                    .setIsSessionIdValid(false)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}

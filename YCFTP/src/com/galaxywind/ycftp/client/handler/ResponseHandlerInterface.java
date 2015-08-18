package com.galaxywind.ycftp.client.handler;

import java.io.IOException;
import java.net.URI;

import org.json.JSONObject;

import com.galaxywind.ycftp.protocal.ResponseObject;


public interface ResponseHandlerInterface {

    void sendResponseMessage(JSONObject response) throws IOException;

    void sendStartMessage();

    void sendFinishMessage();

    void sendProgressMessage(int bytesWritten, int bytesTotal);

    void sendCancelMessage();

    void sendSuccessMessage(int statusCode, byte[] responseBody);

    void sendFailureMessage(int statusCode, byte[] responseBody, Throwable error);

    void sendRetryMessage(int retryNo);

    public URI getRequestURI();

    public void setRequestURI(URI requestURI);

    void setUseSynchronousMode(boolean useSynchronousMode);

    boolean getUseSynchronousMode();

    void onPreProcessResponse(ResponseHandlerInterface instance, ResponseObject response);

    void onPostProcessResponse(ResponseHandlerInterface instance, ResponseObject response);
    
}

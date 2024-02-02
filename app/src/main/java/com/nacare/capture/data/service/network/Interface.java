package com.nacare.capture.data.service.network;

import com.nacare.capture.data.response.ProgramResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Interface {
    @GET("/api/40/programs")
    Call<ProgramResponse> loadProgram(
            @Query("fields") String fields,// =""
            @Query("filter") String filter// ="name:ilike:notification"
    );
}

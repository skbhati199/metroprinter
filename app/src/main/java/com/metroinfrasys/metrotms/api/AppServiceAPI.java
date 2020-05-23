package com.metroinfrasys.metrotms.api;


import com.metroinfrasys.metrotms.data.fastagProcess.FastagProcessAPI;
import com.metroinfrasys.metrotms.data.getFareDetails.GetFareDetailsAPI;
import com.metroinfrasys.metrotms.data.getTransaction.GetTransactionIdAPI;
import com.metroinfrasys.metrotms.data.googlepay.QRCodeCreateAPI;
import com.metroinfrasys.metrotms.data.login.LoginModel;
import com.metroinfrasys.metrotms.data.laneprocess.LaneProcessModel;
import com.metroinfrasys.metrotms.data.logout.LogoutModel;
import com.metroinfrasys.metrotms.data.server.ServerModel;
import com.metroinfrasys.metrotms.data.supervisor.SupervisorModel;
import com.metroinfrasys.metrotms.data.validate.TransactionValidateAPI;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface AppServiceAPI {

    // TC Login API
    @FormUrlEncoded
    @POST("/Api/Login")
    Call<LoginModel> login
    (@Field("LoginID") String loginID,
     @Field("Password") String password,
     @Field("ToLane") String ToLane,
     @Field("LaneID") String laneID,
     @Field("SealNo") String sealNumber);

    // Supervisor Login API
    @FormUrlEncoded
    @POST("/Api/Supervisor")
    Call<SupervisorModel> supervisorLogin
    (@Field("LoginID") String loginID,
     @Field("Password") String password,
     @Field("SealNo") String sealNumber,
     @Field("ToLane") String toLane,
     @Field("LaneID") String laneId,
     @Field("PlazaID") String plazaId);

    // Fastag Transaction API
    @FormUrlEncoded
    @POST("Api/CCHBlackListcheck")
    Call<FastagProcessAPI> fastagProcess
    (@Field("PlazaID") String PlazaID,
     @Field("LaneID") String LaneID,
     @Field("TagId") String TagId,
     @Field("TagVehicleClassification") String TagVehicleClassification,
     @Field("CCHID") String CCHID,
     @Field("TagReadDateTime") String TagReadDateTime,
     @Field("TollZoneID") String TollZoneID);

    // Lane Process API
    @FormUrlEncoded
    @POST("/Api/lane")
    Call<LaneProcessModel> laneprocess
    (@Field("LaneID") String laneID,
     @Field("PlazaID") String plazaID,
     @Field("ReceiptValue") String receiptValue,
     @Field("ShiftNumber") String shiftNumber,
     @Field("LoginID") String loginID,
     @Field("ClassID") String classID,
     @Field("SealNo") String sealNo);


    // Get Plaza Fare API
    @GET("/Api/Fare")
    Call<GetFareDetailsAPI> getFare();

   // Get Server Status API
    @GET("/Api/Login")
    Call<ServerModel> getServerStatus();

    // Logout API
    @FormUrlEncoded
    @POST("/Api/Logout")
    Call<LogoutModel> logout
            (@Field("LaneID") String laneID,
             @Field("PlazaID") String plazaID,
             @Field("SealNo") String sealNo);

    // validate transaction
    @FormUrlEncoded
    @POST("/Api/merchantQrPaymentsvalidate")
    Call<TransactionValidateAPI> validateTransaction
    (@Field("transactionId") String transactionId,
     @Field("paymentType") String paymentType);


    // generate QR Code
    @POST
    Call<QRCodeCreateAPI> createqrcode(
            @Url String url,
            @Header("Authorization") String token,
            @Body String fasTagJsonOtpRequest
    );

    // Get Transaction Id & QR Code
    @FormUrlEncoded
    @POST("Api/merchantQrPaymentscreate")
    Call<GetTransactionIdAPI> getTransactoinId
            (@Field("UserID") String UserID,
             @Field("ShiftNo") String ShiftNo,
             @Field("FareAmount") String FareAmount,
            @Field("LaneID") String LaneID,
             @Field("PlazaID") String PlazaID,
             @Field("SealNo") String SealNo,
             @Field("VehicleClass") String VehicleClass,
             @Field("PaymentType") String PaymentType);

}

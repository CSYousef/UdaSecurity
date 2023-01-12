module ImageService {
    exports com.udacity.catpoint.ImageService.service;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.services.rekognition;
    requires software.amazon.awssdk.regions;
    requires java.desktop;
    requires slf4j.api;

}
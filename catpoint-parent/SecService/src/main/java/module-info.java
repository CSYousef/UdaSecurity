module SecService {
    requires ImageService;
    opens com.udacity.catpoint.data to com.google.gson;
    requires miglayout.swing;
    requires java.desktop;
    requires guava;
    requires com.google.gson;
    requires java.prefs;

}
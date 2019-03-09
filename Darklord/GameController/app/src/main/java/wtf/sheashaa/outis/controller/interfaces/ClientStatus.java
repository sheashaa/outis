package wtf.sheashaa.outis.controller.interfaces;

public interface ClientStatus {
    void onDisconnected();
    void onConnected();
    void onConnecting();
}

package tfud.client;

import java.io.IOException;

public class TestClient {

    private tfud.client.ChatClientFrame client;

    public TestClient() throws IOException {
        ChatClient t = new ChatClient("localhost", 8900);

        client = new tfud.client.ChatClientFrame(t);
    }

    public static void main(String[] args) throws IOException {
        TestClient tc = new TestClient();
    }

}

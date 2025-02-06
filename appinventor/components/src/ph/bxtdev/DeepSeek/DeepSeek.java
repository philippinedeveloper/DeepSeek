package ph.bxtdev.DeepSeek;

import android.app.Activity;
import android.content.Context;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@DesignerComponent(
        version = 1,
        description = "DeepSeek AI Chat Extension",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "aiwebres/icon.png")
@SimpleObject(external = true)
// Libraries
@UsesLibraries(libraries = "")
// Permissions
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public class DeepSeek extends AndroidNonvisibleComponent {

    private Context context;
    private Activity activity;
    private static final String API_URL = "https://api.deepseek.com/chat/completions";
    private static final String API_KEY;

    public DeepSeek(ComponentContainer container){
        super(container.$form());
        this.activity = container.$context();
        this.context = container.$context();
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXTAREA, defaultValue = "YOUR_API_KEY")
    @SimpleProperty(description = "Sets API key to DeepSeek", category = PropertyCategory.ADVANCED)
    public void ApiKey(String key){
            key = API_KEY;
    }

    @SimpleProperty(description = "Gets API key from DeepSeek", category = PropertyCategory.ADVANCED)
    public String ApiKey(){
           return API_KEY;
    }
        

    @SimpleFunction(description = "Sends a message to DeepSeek and returns the response")
    public void Send(String message){
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run (){
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
                connection.setDoOutput(true);

                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("model", "deepseek-chat");
                jsonRequest.put("frequency_penalty", 0);
                jsonRequest.put("max_tokens", 2048);
                jsonRequest.put("presence_penalty", 0);
                JSONObject responseFormat = new JSONObject();
                responseFormat.put("type", "text");
                jsonRequest.put("response_format", responseFormat);
                jsonRequest.put("stop", JSONObject.NULL);
                jsonRequest.put("stream", false);
                jsonRequest.put("stream_options", JSONObject.NULL);
                jsonRequest.put("temperature", 1);
                jsonRequest.put("top_p", 1);
                jsonRequest.put("tools", JSONObject.NULL);
                jsonRequest.put("tool_choice", "none");
                jsonRequest.put("logprobs", false);
                jsonRequest.put("top_logprobs", JSONObject.NULL);

                JSONArray messages = new JSONArray();
                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");
                userMessage.put("content", message);
                messages.put(userMessage);
                jsonRequest.put("messages", messages);

                OutputStream os = connection.getOutputStream();
                os.write(jsonRequest.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    String responseText = response.toString();
                    GotResponse(responseText);
                } else {
                    Error();
                }
                connection.disconnect();
            } 
        });
    }

    @SimpleEvent(description = "Triggered when a response is received from DeepSeek")
    public void GotResponse(String response) {
        EventDispatcher.dispatchEvent(this, "GotResponse", response);
    }

    @SimpleEvent(description = "Triggered when a response failed")
    public void Error() {
        EventDispatcher.dispatchEvent(this, "Error");
    }
}

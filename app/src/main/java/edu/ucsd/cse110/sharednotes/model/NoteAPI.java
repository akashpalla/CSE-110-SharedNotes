package edu.ucsd.cse110.sharednotes.model;

import android.util.Log;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;

import androidx.lifecycle.LiveData;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NoteAPI {
    // TODO: Implement the API using OkHttp!
    // TODO: - getNote (maybe getNoteAsync)
    // TODO: - putNote (don't need putNotAsync, probably)
    // TODO: Read the docs: https://square.github.io/okhttp/
    // TODO: Read the docs: https://sharednotes.goto.ucsd.edu/docs

    private volatile static NoteAPI instance = null;
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private OkHttpClient client;

    public NoteAPI() {
        this.client = new OkHttpClient();
    }

    public static NoteAPI provide() {
        if (instance == null) {
            instance = new NoteAPI();
        }
        return instance;
    }

    /**
     * An example of sending a GET request to the server.
     * <p>
     * The /echo/{msg} endpoint always just returns {"message": msg}.
     *
     * This method should can be called on a background thread (Android
     * disallows network requests on the main thread).
     */
    @WorkerThread
    public String echo(String msg) {
        // URLs cannot contain spaces, so we replace them with %20.
        String encodedMsg = msg.replace(" ", "%20");

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/echo/" + encodedMsg)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            Log.i("ECHO", body);
            return body;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @AnyThread
    public Future<String> echoAsync(String msg) {
        var executor = Executors.newSingleThreadExecutor();
        var future = executor.submit(() -> echo(msg));
        // We can use future.get(1, SECONDS) to wait for the result.
        return future;
    }

    public Note getNoteByTitle(String title) {
        // URLs cannot contain spaces, so we replace them with %20.
        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + title)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            var body = response.body().string();
            Log.i("get",   body);
            return Note.fromJSON(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @AnyThread
    public Note getNoteAsynch(String title)  {
        var executor = Executors.newSingleThreadExecutor();
        var future = executor.submit(() -> getNoteByTitle(title));
        try {
            return future.get(1, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    ;    return null;
    }

    public void pushNote(Note note){
        RequestBody body = RequestBody.create(note.toJSON(), JSON);

        Log.i("push", note.toJSON());

        Request request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + note.title)
                .method("PUT", body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            Log.i("push", String.valueOf(response.isSuccessful()));

            Log.i("push",   response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @AnyThread
    public void pushNoteAsynch(Note note){
        var executor = Executors.newSingleThreadExecutor();
        var future = executor.submit(() -> pushNote(note));

    }



}

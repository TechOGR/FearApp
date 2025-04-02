package com.onelcrack.hellodear;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class SendMessage extends AsyncTask {
    @Override
    protected Object doInBackground(Object[] objects) {
        String ip = (String) objects[0];
        int port = (int) objects[1];
        String message = (String) objects[2];

        try {
            Socket socket = new Socket(ip,port);
            OutputStream outputStream = socket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            dataOutputStream.writeUTF(message);

            socket.close();

            return "ENviado";
        } catch (IOException e){
            return "ERROR";
        }
    }
    @Override
    protected void onPostExecute(Object o) {
        Log.d("Mensaje enviado", (String) o);
    }
}

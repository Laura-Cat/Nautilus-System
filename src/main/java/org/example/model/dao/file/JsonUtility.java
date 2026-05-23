package org.example.model.dao.file;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.InstanceCreator;
import org.example.model.domain.User;
import org.example.model.domain.Cliente;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.example.model.domain.TitoloAccesso;
import org.example.model.domain.PacchettoCrediti;
import org.example.model.domain.AbbonamentoPeriodico;

public class JsonUtility {

    // 1. Adattatore per le date semplici (es. Data di Nascita)
    private static final TypeAdapter<LocalDate> localDateTypeAdapter = new TypeAdapter<LocalDate>() {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(formatter.format(value));
            }
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDate.parse(in.nextString(), formatter);
        }
    };

    // 2. 🌟 NUOVO: Adattatore per Date + Orario (es. Invio Notifiche)
    private static final TypeAdapter<LocalDateTime> localDateTimeTypeAdapter = new TypeAdapter<LocalDateTime>() {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(formatter.format(value));
            }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDateTime.parse(in.nextString(), formatter);
        }
    };

    // 3. Registriamo ENTRAMBI gli adattatori in Gson
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class, localDateTypeAdapter)
            .registerTypeAdapter(LocalDateTime.class, localDateTimeTypeAdapter)
            .registerTypeAdapter(User.class, (InstanceCreator<User>) type -> new Cliente(null, null, null, null, null, null, null, null, null, null, null))
            .registerTypeAdapter(TitoloAccesso.class, (JsonDeserializer<TitoloAccesso>) (json, typeOfT, context) -> {
                JsonObject obj = json.getAsJsonObject();
                if (obj.has("creditiRimanenti")) {
                    return context.deserialize(json, PacchettoCrediti.class);
                } else {
                    return context.deserialize(json, AbbonamentoPeriodico.class);
                }
            })
            .create();

    // ==============================================================================
    // METODI DI LETTURA E SCRITTURA
    // ==============================================================================

    public static <T> List<T> leggiLista(String percorsoFile, Type typeToken) {
        File file = new File(percorsoFile);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(file)) {
            List<T> lista = gson.fromJson(reader, typeToken);
            return lista != null ? lista : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static <T> void scriviLista(String percorsoFile, List<T> lista) {
        try (Writer writer = new FileWriter(percorsoFile)) {
            gson.toJson(lista, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
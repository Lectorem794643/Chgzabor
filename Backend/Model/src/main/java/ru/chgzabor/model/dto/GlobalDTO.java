package ru.chgzabor.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.io.IOException;

@Data
public class GlobalDTO {
    public String id;
    public String drawingName; // Имя чертежа
    public String orderNumber; // Номер закза

    @JsonProperty("L") public int L;
    @JsonProperty("H") public int H;
    public int pillarCount; // Количество стоблов
    public String beam; // Балка

    @JsonProperty("B") public int B;
    @JsonProperty("E") public int E;
    @JsonProperty("C") public int C;
    @JsonProperty("D") public int D;
    @JsonProperty("A") public int A;
    public int pillarHeight; // Высота столбов

    public String pillarRectangularFlange; // Столб на прямоугольном фланце
    public String pillarType; // Вид столба
    public String typeFilling; // Вид заполнения
    public String colorRal1;
    public String colorRal2;
    public String comment;

    public int groundIndent; // Отступ от грунта
    public int gapAdjustment; // Коэффициент регулировки зазора
    public String pipe; // Труба

    public static GlobalDTO fromJsonString(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonString, GlobalDTO.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

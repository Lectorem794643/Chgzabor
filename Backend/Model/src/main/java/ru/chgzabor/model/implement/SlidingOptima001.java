package ru.chgzabor.model.implement;

import lombok.extern.slf4j.Slf4j;
import ru.chgzabor.model.dto.GlobalDTO;

import java.io.IOException;

@Slf4j
public class SlidingOptima001 {
    private final GlobalDTO model;

    public SlidingOptima001(GlobalDTO model) throws IOException {
        this.model = model;
    }

    public GlobalDTO calculatModel() {
        model.id = "SlidingOptima001";

        // Главная конструкционная труба
        int pipe = switch (model.pipe) {
            case ("30") -> 30;
            case ("40") -> 40;
            case ("60") -> 60;
            case ("80") -> 80;
            default -> 0;
        };

        // Балка
        int beam = switch (model.beam) {
            case ("МИКРО 60х55 mm") -> 55;
            case ("ЭКО 60х70 mm") -> 60;
            case ("ЕВРО 90х75 mm") -> 75;
            case ("МАКС 135х130 mm") -> 130;
            default -> 0;
        };

        int groundIndent = model.groundIndent; // Отступ от грунта
        int gapAdjustment = model.gapAdjustment; // Зазор

        // Рассчет парметров L-группы      |
        // --------------------------------|
        model.B = (model.L + gapAdjustment);
        model.A = (model.L / 2 * 3);
        model.E = (model.B - pipe * 3) / 2;

        // Рассчет парметров H-группы      |
        // --------------------------------|
        model.C = (model.H - groundIndent);
        model.D = (model.H - groundIndent - beam - pipe * 2);

        // Вычисляем высоту столбов
        if (model.H == 0) {
            model.pillarHeight = 0;
        } else if (model.H <= 2200) {
            model.pillarHeight = 3000;
        } else {
            model.pillarHeight = 4000;
        }

        return model;
    }
}

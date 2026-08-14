package ru.ssau.srestapp.dto.event;

import lombok.Data;

import java.util.List;

//какие поля изменяем от админа инфа
@Data
public class ApproveChangesDto {
    private List<String> fields;
    private Boolean applyAll;
}

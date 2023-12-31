package de.fayedev.andurilwebdocker.model;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AndurilFile {

    private String name;
    private String buildName;
    private List<String> logs;
}

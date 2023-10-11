package de.fayedev.andurilwebdocker.model;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AndurilFeatureFlag {

    private String fileName;
    private Integer line;
    private String name;
    private String value;
    private boolean isDefined;
}

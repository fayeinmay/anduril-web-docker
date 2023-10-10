package de.fayedev.andurilwebdocker.model;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AndurilFeatureFlag {

    private String fileName;
    private String name;
    private String value;
    private boolean isDefined;
}

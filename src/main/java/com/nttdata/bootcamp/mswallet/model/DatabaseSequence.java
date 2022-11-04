package com.nttdata.bootcamp.mswallet.model;

import com.mongodb.lang.NonNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "db_sequence")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DatabaseSequence {

    @Id
    private String id;

    @NonNull
    private Long seq;

}


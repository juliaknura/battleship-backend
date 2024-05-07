package com.backend.battleship.service;

import com.backend.battleship.model.Rank;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class DatabaseService {

    private static final List<Rank> ranking = Arrays.asList(new Rank("fillerNick1",10),new Rank("fillerNick5",10), new Rank("fillerNick2",9), new Rank("fillerNick4",8), new Rank("fillerNick3",7),new Rank("fillerNick6",6),new Rank("fillerNick8",5), new Rank("fillerNick7",4), new Rank("fillerNick9",3), new Rank("fillerNick10",3),new Rank("fillerNick11",2));

    public List<Rank> getTop10()
    {
        return ranking.stream().sorted(Comparator.reverseOrder()).limit(10).toList();
    }

    public void saveGameResult()
    {
        //TODO implement
    }
}

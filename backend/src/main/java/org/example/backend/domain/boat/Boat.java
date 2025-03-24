package org.example.backend.domain.boat;

import org.example.backend.domain.User.UserId;

import java.util.Set;

public class Boat {
    private BoatId id;
    private String name;
    private Set<UserId> owners;

    public Boat(BoatId id, String name, Set<UserId> owners) {
        this.id = id;
        this.name = name;
        this.owners = owners;
    }

    public boolean isCoOwner(UserId userId) {
        return owners.contains(userId);
    }
}


package de.jensnistler.routemap.fragments;

import de.jensnistler.routemap.helper.TrackModel;

public class TrackListOwn extends TrackListAbstract {
    protected Integer getType() {
        return TrackModel.TYPE_MY_TRACKS;
    }
}

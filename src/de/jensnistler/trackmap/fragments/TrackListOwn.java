package de.jensnistler.trackmap.fragments;

import de.jensnistler.trackmap.helper.TrackModel;

public class TrackListOwn extends TrackListAbstract {
    protected Integer getType() {
        return TrackModel.TYPE_MY_TRACKS;
    }
}

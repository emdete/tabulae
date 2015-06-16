package org.pyneo.maps.track;

oneway interface ITrackWriterCallback {
    /**
     * Called when the service has a new value for you.
     */
    void newPointWritten(double lat, double lon);
    void onTrackStatUpdate(int Cnt, double Distance, long Duration, double MaxSpeed, double AvgSpeed, long MoveTime, double AvgMoveSpeed);
}

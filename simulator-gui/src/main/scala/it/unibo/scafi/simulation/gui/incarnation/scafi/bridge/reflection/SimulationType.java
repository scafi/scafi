package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.reflection;

/**
 * annotations purpose, it is a wrapper of simulation profile that make possible to
 * mark a simulation with a specific simulation profile.
 */
public enum SimulationType {
    /**
     * standard scafi simulation, there is a set of input sensor (sensor1,2,3) that can be modified
     * with the use of ToggleDevice command.
     */
    STANDARD(SimulationProfile.standardProfile$.MODULE$),
    /**
     * this simulation profile has only one sensor and produced a general output
     */
    ON_OFF_INPUT_ANY_OUTPUT(SimulationProfile.onOffInputAnyOutput$.MODULE$),
    /**
     * a movement simulation
     */
    MOVEMENT((SimulationProfile.movementProfile$.MODULE$));

    private final SimulationProfile _profile;

    SimulationType(final SimulationProfile profile) {
        this._profile = profile;
    }

    public SimulationProfile profile() {
        return this._profile;
    }
}

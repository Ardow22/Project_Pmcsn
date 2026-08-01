package logic.PMCSN.model;

import java.util.ArrayList;
import java.util.List;

public class TransientStats {
	private List<Double> transientStatsLogin;
	private List<Double> transientStatsUT;
	private List<Double> transientStatsStagioni;
	private List<Double> transientStatsClub;
	
	public TransientStats() {
		this.setTransientStatsLogin(new ArrayList<>());
		this.setTransientStatsUT(new ArrayList<>());
		this.setTransientStatsStagioni(new ArrayList<>());
		this.setTransientStatsClub(new ArrayList<>());
	}

	public List<Double> getTransientStatsLogin() {
		return transientStatsLogin;
	}

	public void setTransientStatsLogin(List<Double> transientStatsLogin) {
		this.transientStatsLogin = transientStatsLogin;
	}

	public List<Double> getTransientStatsUT() {
		return transientStatsUT;
	}

	public void setTransientStatsUT(List<Double> transientStatsUT) {
		this.transientStatsUT = transientStatsUT;
	}

	public List<Double> getTransientStatsStagioni() {
		return transientStatsStagioni;
	}

	public void setTransientStatsStagioni(List<Double> transientStatsStagioni) {
		this.transientStatsStagioni = transientStatsStagioni;
	}

	public List<Double> getTransientStatsClub() {
		return transientStatsClub;
	}

	public void setTransientStatsClub(List<Double> transientStatsClub) {
		this.transientStatsClub = transientStatsClub;
	}
	

}

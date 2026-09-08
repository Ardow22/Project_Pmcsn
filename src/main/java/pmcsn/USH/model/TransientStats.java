package pmcsn.USH.model;

import java.util.ArrayList;
import java.util.List;

public class TransientStats {
	private List<Double> transientStatsSicurezza;
	private List<Double> transientStatsBiglietteria;
	private List<Double> transientStatsControlli;
	private List<Double> transientStatsMarioStandard;
	private List<Double> transientStatsMarioExpress;
	private List<Double> transientStatsHPStandard;
	private List<Double> transientStatsHPExpress;
	private List<Double> transientStatsMario;
	private List<Double> transientStatsHP;
	
	public TransientStats() {
		this.setTransientStatsSicurezza(new ArrayList<>());
		this.setTransientStatsBiglietteria(new ArrayList<>());
		this.setTransientStatsControlli(new ArrayList<>());
		this.setTransientStatsMarioStandard(new ArrayList<>());
		this.setTransientStatsMarioExpress(new ArrayList<>());
		this.setTransientStatsHPStandard(new ArrayList<>());
		this.setTransientStatsHPExpress(new ArrayList<>());
		this.setTransientStatsMario(new ArrayList<>());
		this.setTransientStatsHP(new ArrayList<>());
	}

	public List<Double> getTransientStatsSicurezza() {
		return transientStatsSicurezza;
	}

	public void setTransientStatsSicurezza(List<Double> transientStatsSicurezza) {
		this.transientStatsSicurezza = transientStatsSicurezza;
	}

	public List<Double> getTransientStatsBiglietteria() {
		return transientStatsBiglietteria;
	}

	public void setTransientStatsBiglietteria(List<Double> transientStatsBiglietteria) {
		this.transientStatsBiglietteria = transientStatsBiglietteria;
	}

	public List<Double> getTransientStatsControlli() {
		return transientStatsControlli;
	}

	public void setTransientStatsControlli(List<Double> transientStatsControlli) {
		this.transientStatsControlli = transientStatsControlli;
	}

	public List<Double> getTransientStatsMarioStandard() {
		return transientStatsMarioStandard;
	}

	public void setTransientStatsMarioStandard(List<Double> transientStatsMarioStandard) {
		this.transientStatsMarioStandard = transientStatsMarioStandard;
	}
	
	public List<Double> getTransientStatsMarioExpress() {
		return transientStatsMarioExpress;
	}

	public void setTransientStatsMarioExpress(List<Double> transientStatsMarioExpress) {
		this.transientStatsMarioExpress = transientStatsMarioExpress;
	}
	
	public List<Double> getTransientStatsHPStandard() {
		return transientStatsHPStandard;
	}

	public void setTransientStatsHPStandard(List<Double> transientStatsHPStandard) {
		this.transientStatsHPStandard = transientStatsHPStandard;
	}
	
	public List<Double> getTransientStatsHPExpress() {
		return transientStatsHPExpress;
	}

	public void setTransientStatsHPExpress(List<Double> transientStatsHPExpress) {
		this.transientStatsHPExpress = transientStatsHPExpress;
	}
	
	public List<Double> getTransientStatsMario() {
		return transientStatsMario;
	}

	public void setTransientStatsMario(List<Double> transientStatsMario) {
		this.transientStatsMario = transientStatsMario;
	}
	
	public List<Double> getTransientStatsHP() {
		return transientStatsHP;
	}

	public void setTransientStatsHP(List<Double> transientStatsHP) {
		this.transientStatsHP = transientStatsHP;
	}


}

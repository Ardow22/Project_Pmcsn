package logic.PMCSN.model;

import java.util.ArrayList;
import java.util.List;

public abstract class Node {
	protected List<Double> tempiMediDiRispostaBatch;
	protected List<Double> tempiMediInCodaBatch;
	protected List<Double> tempiDiServizioBatch;
	protected List<Double> popolazioneDelSistemaBatch;
	protected List<Double> popolazioneDellaCodaBatch;
	protected List<Double> utilizzazioneBatch;
	protected List<Double> interarriviBatch;
	protected int streamIndex;
	protected double serviceTime;
	protected double currentStartTimeBatch;
	protected double lastCompletionTime;
	protected double currentFirstArrivalTime;
	
	protected Node(int streamIndex, double serviceTime) {
		this.streamIndex = streamIndex;
		this.serviceTime = serviceTime;
		this.currentStartTimeBatch = 0.0;
		this.currentFirstArrivalTime = 0.0;
		
		this.tempiMediDiRispostaBatch = new ArrayList<>();
		this.tempiMediInCodaBatch = new ArrayList<>();
		this.tempiDiServizioBatch = new ArrayList<>();
		this.popolazioneDelSistemaBatch = new ArrayList<>();
		this.popolazioneDellaCodaBatch = new ArrayList<>();
		this.utilizzazioneBatch = new ArrayList<>();
		this.interarriviBatch = new ArrayList<>();
	}
	
	public List<Double> getTempiMediDiRispostaBatch() {
		return tempiMediDiRispostaBatch;
	}
	public void setTempiMediDiRispostaBatch(List<Double> tempiMediDiRispostaBatch) {
		this.tempiMediDiRispostaBatch = tempiMediDiRispostaBatch;
	}
	public List<Double> getTempiMediInCodaBatch() {
		return tempiMediInCodaBatch;
	}
	public void setTempiMediInCodaBatch(List<Double> tempiMediInCodaBatch) {
		this.tempiMediInCodaBatch = tempiMediInCodaBatch;
	}
	public List<Double> getTempiDiServizioBatch() {
		return tempiDiServizioBatch;
	}
	public void setTempiDiServizioBatch(List<Double> tempiDiServizioBatch) {
		this.tempiDiServizioBatch = tempiDiServizioBatch;
	}
	public List<Double> getPopolazioneDelSistemaBatch() {
		return popolazioneDelSistemaBatch;
	}
	public void setPopolazioneDelSistemaBatch(List<Double> popolazioneDelSistemaBatch) {
		this.popolazioneDelSistemaBatch = popolazioneDelSistemaBatch;
	}
	public List<Double> getPopolazioneDellaCodaBatch() {
		return popolazioneDellaCodaBatch;
	}
	public void setPopolazioneDellaCodaBatch(List<Double> popolazioneDellaCodaBatch) {
		this.popolazioneDellaCodaBatch = popolazioneDellaCodaBatch;
	}
	public List<Double> getUtilizzazioneBatch() {
		return utilizzazioneBatch;
	}
	public void setUtilizzazioneBatch(List<Double> utilizzazioneBatch) {
		this.utilizzazioneBatch = utilizzazioneBatch;
	}
	public List<Double> getInterarriviBatch() {
		return interarriviBatch;
	}
	public void setInterarriviBatch(List<Double> interarriviBatch) {
		this.interarriviBatch = interarriviBatch;
	}
	public int getStreamIndex() {
		return streamIndex;
	}
	public void setStreamIndex(int streamIndex) {
		this.streamIndex = streamIndex;
	}
	public double getServiceTime() {
		return serviceTime;
	}
	public void setServiceTime(double serviceTime) {
		this.serviceTime = serviceTime;
	}
	public double getCurrentStartTimeBatch() {
		return currentStartTimeBatch;
	}
	public void setCurrentStartTimeBatch(double currentStartTimeBatch) {
		this.currentStartTimeBatch = currentStartTimeBatch;
	}
	public void setLastCompletionTime(double lastCompletionTime) {
		this.lastCompletionTime = lastCompletionTime;
	}
	public double getCurrentFirstArrivalTime() {
		return currentFirstArrivalTime;
	}
	public void setCurrentFirstArrivalTime(double currentFirstArrivalTime) {
		this.currentFirstArrivalTime = currentFirstArrivalTime;
	}

}

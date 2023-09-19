package org.openmrs.mobile.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Table(name = "covidtbintegrator")
public class CovidTBIntegrator extends Model implements Serializable {


    @Column(name = "patientuuid")
    @SerializedName("patientuuid")
    @Expose
    private String patientuuid;

    @Column(name = "blood_transfusion")
    @SerializedName("blood_transfusion")
    @Expose
    private String blood_transfusion;

    @Column(name = "unprotected_sex")
    @SerializedName("unprotected_sex")
    @Expose
    private String unprotected_sex;

    @Column(name = "sti")
    @SerializedName("sti")
    @Expose
    private String sti;

    @Column(name = "diagnosed_tb")
    @SerializedName("diagnosed_tb")
    @Expose
    private String diagnosed_tb;

    @Column(name = "iv_drugs")
    @SerializedName("iv_drugs")
    @Expose
    private String iv_drugs;

    @Column(name = "forced_sex")
    @SerializedName("forced_sex")
    @Expose
    private String forced_sex;

    @Column(name = "complaints_genital_sores")
    @SerializedName("complaints_genital_sores")
    @Expose
    private String complaints_genital_sores;

    //Clinical TB Screening Starts here

    @Column(name = "cough_gt_2wks")
    @SerializedName("cough_gt_2wks")
    @Expose
    private String cough_gt_2wks;

    @Column(name = "weight_loss")
    @SerializedName("weight_loss")
    @Expose
    private String weight_loss;

    @Column(name = "fever")
    @SerializedName("fever")
    @Expose
    private String fever;

    @Column(name = "night_sweats")
    @SerializedName("night_sweats")
    @Expose
    private String night_sweats;

    @Column(name = "cough_w_heamoptysis")
    @SerializedName("cough_w_heamoptysis")
    @Expose
    private String cough_w_heamoptysis;

    @Column(name = "cough_weightloss_fever")
    @SerializedName("cough_weightloss_fever")
    @Expose
    private String cough_weightloss_fever;

    @Column(name = "cough_unexplained_weightloss")
    @SerializedName("cough_unexplained_weightloss")
    @Expose
    private String cough_unexplained_weightloss;

    @Column(name = "cough_w_fever_gt_2wks")
    @SerializedName("cough_w_fever_gt_2wks")
    @Expose
    private String cough_w_fever_gt_2wks;

    @Column(name = "unexplained_weightloss")
    @SerializedName("unexplained_weightloss")
    @Expose
    private String unexplained_weightloss;

    @Column(name = "cough_sputum")
    @SerializedName("cough_sputum")
    @Expose
    private String cough_sputum;

    //Clinical Covid-19 Screening

    @Column(name = "temperature_reading")
    @SerializedName("temperature_reading")
    @Expose
    private String temperature_reading;

    @Column(name = "health_care_worker")
    @SerializedName("health_care_worker")
    @Expose
    private String health_care_worker;

    @Column(name = "dry_cough")
    @SerializedName("dry_cough")
    @Expose
    private String dry_cough;

    @Column(name = "shortness_of_breath")
    @SerializedName("shortness_of_breath")
    @Expose
    private String shortness_of_breath;

    @Column(name = "history_of_fever")
    @SerializedName("history_of_fever")
    @Expose
    private String history_of_fever;

    @Column(name = "muscle_aches")
    @SerializedName("muscle_aches")
    @Expose
    private String muscle_aches;

    @Column(name = "not_vaccinated")
    @SerializedName("not_vaccinated")
    @Expose
    private String not_vaccinated;

    @Column(name = "loss_of_taste")
    @SerializedName("loss_of_taste")
    @Expose
    private String loss_of_taste;

    @Column(name = "loss_of_sense")
    @SerializedName("loss_of_sense")
    @Expose
    private String loss_of_sense;

    @Column(name = "sore_throat")
    @SerializedName("sore_throat")
    @Expose
    private String sore_throat;

    @Column(name = "headache")
    @SerializedName("headache")
    @Expose
    private String headache;

    @Column(name = "international_travel")
    @SerializedName("international_travel")
    @Expose
    private String international_travel;

    @Column(name = "close_contact")
    @SerializedName("close_contact")
    @Expose
    private String close_contact;

    @Column(name = "history_of_chronic")
    @SerializedName("history_of_chronic")
    @Expose
    private String history_of_chronic;

    public String getPatientuuid() {
        return patientuuid;
    }

    public void setPatientuuid(String patientuuid) {
        this.patientuuid = patientuuid;
    }

    public String getBlood_transfusion() {
        return blood_transfusion;
    }

    public void setBlood_transfusion(String blood_transfusion) {
        this.blood_transfusion = blood_transfusion;
    }

    public String getUnprotected_sex() {
        return unprotected_sex;
    }

    public void setUnprotected_sex(String unprotected_sex) {
        this.unprotected_sex = unprotected_sex;
    }

    public String getSti() {
        return sti;
    }

    public void setSti(String sti) {
        this.sti = sti;
    }

    public String getDiagnosed_tb() {
        return diagnosed_tb;
    }

    public void setDiagnosed_tb(String diagnosed_tb) {
        this.diagnosed_tb = diagnosed_tb;
    }

    public String getIv_drugs() {
        return iv_drugs;
    }

    public void setIv_drugs(String iv_drugs) {
        this.iv_drugs = iv_drugs;
    }

    public String getForced_sex() {
        return forced_sex;
    }

    public void setForced_sex(String forced_sex) {
        this.forced_sex = forced_sex;
    }

    public String getComplaints_genital_sores() {
        return complaints_genital_sores;
    }

    public void setComplaints_genital_sores(String complaints_genital_sores) {
        this.complaints_genital_sores = complaints_genital_sores;
    }

    public String getCough_gt_2wks() {
        return cough_gt_2wks;
    }

    public void setCough_gt_2wks(String cough_gt_2wks) {
        this.cough_gt_2wks = cough_gt_2wks;
    }

    public String getWeight_loss() {
        return weight_loss;
    }

    public void setWeight_loss(String weight_loss) {
        this.weight_loss = weight_loss;
    }

    public String getFever() {
        return fever;
    }

    public void setFever(String fever) {
        this.fever = fever;
    }

    public String getNight_sweats() {
        return night_sweats;
    }

    public void setNight_sweats(String night_sweats) {
        this.night_sweats = night_sweats;
    }

    public String getCough_w_heamoptysis() {
        return cough_w_heamoptysis;
    }

    public void setCough_w_heamoptysis(String cough_w_heamoptysis) {
        this.cough_w_heamoptysis = cough_w_heamoptysis;
    }

    public String getCough_weightloss_fever() {
        return cough_weightloss_fever;
    }

    public void setCough_weightloss_fever(String cough_weightloss_fever) {
        this.cough_weightloss_fever = cough_weightloss_fever;
    }

    public String getCough_unexplained_weightloss() {
        return cough_unexplained_weightloss;
    }

    public void setCough_unexplained_weightloss(String cough_unexplained_weightloss) {
        this.cough_unexplained_weightloss = cough_unexplained_weightloss;
    }

    public String getCough_w_fever_gt_2wks() {
        return cough_w_fever_gt_2wks;
    }

    public void setCough_w_fever_gt_2wks(String cough_w_fever_gt_2wks) {
        this.cough_w_fever_gt_2wks = cough_w_fever_gt_2wks;
    }

    public String getUnexplained_weightloss() {
        return unexplained_weightloss;
    }

    public void setUnexplained_weightloss(String unexplained_weightloss) {
        this.unexplained_weightloss = unexplained_weightloss;
    }

    public String getCough_sputum() {
        return cough_sputum;
    }

    public void setCough_sputum(String cough_sputum) {
        this.cough_sputum = cough_sputum;
    }

    public String getTemperature_reading() {
        return temperature_reading;
    }

    public void setTemperature_reading(String temperature_reading) {
        this.temperature_reading = temperature_reading;
    }

    public String getHealth_care_worker() {
        return health_care_worker;
    }

    public void setHealth_care_worker(String health_care_worker) {
        this.health_care_worker = health_care_worker;
    }

    public String getDry_cough() {
        return dry_cough;
    }

    public void setDry_cough(String dry_cough) {
        this.dry_cough = dry_cough;
    }

    public String getShortness_of_breath() {
        return shortness_of_breath;
    }

    public void setShortness_of_breath(String shortness_of_breath) {
        this.shortness_of_breath = shortness_of_breath;
    }

    public String getHistory_of_fever() {
        return history_of_fever;
    }

    public void setHistory_of_fever(String history_of_fever) {
        this.history_of_fever = history_of_fever;
    }

    public String getMuscle_aches() {
        return muscle_aches;
    }

    public void setMuscle_aches(String muscle_aches) {
        this.muscle_aches = muscle_aches;
    }

    public String getNot_vaccinated() {
        return not_vaccinated;
    }

    public void setNot_vaccinated(String not_vaccinated) {
        this.not_vaccinated = not_vaccinated;
    }

    public String getLoss_of_taste() {
        return loss_of_taste;
    }

    public void setLoss_of_taste(String loss_of_taste) {
        this.loss_of_taste = loss_of_taste;
    }

    public String getLoss_of_sense() {
        return loss_of_sense;
    }

    public void setLoss_of_sense(String loss_of_sense) {
        this.loss_of_sense = loss_of_sense;
    }

    public String getSore_throat() {
        return sore_throat;
    }

    public void setSore_throat(String sore_throat) {
        this.sore_throat = sore_throat;
    }

    public String getHeadache() {
        return headache;
    }

    public void setHeadache(String headache) {
        this.headache = headache;
    }

    public String getInternational_travel() {
        return international_travel;
    }

    public void setInternational_travel(String international_travel) {
        this.international_travel = international_travel;
    }

    public String getClose_contact() {
        return close_contact;
    }

    public void setClose_contact(String close_contact) {
        this.close_contact = close_contact;
    }

    public String getHistory_of_chronic() {
        return history_of_chronic;
    }

    public void setHistory_of_chronic(String history_of_chronic) {
        this.history_of_chronic = history_of_chronic;
    }
}

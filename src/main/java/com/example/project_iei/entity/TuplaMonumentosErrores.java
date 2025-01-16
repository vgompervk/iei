package com.example.project_iei.entity;

import java.util.ArrayList;
import java.util.List;

public class TuplaMonumentosErrores {
    private List<Monumento> monumentos;
    private List<String> fallosReparados;
    private List<String> fallosRechazados;

    public List<Monumento> getMonumentos() {
        return monumentos;
    }

    public void setMonumentos(List<Monumento> monumentos) {
        this.monumentos = monumentos;
    }

    public List<String> getFallosReparados() {
        return fallosReparados;
    }

    public void setFallosReparados(List<String> fallosReparados) {
        this.fallosReparados = fallosReparados;
    }

    public List<String> getFallosRechazados() {
        return fallosRechazados;
    }

    public void setFallosRechazados(List<String> fallosRechazados) {
        this.fallosRechazados = fallosRechazados;
    }
}

package com.carrillovillalvadaas.tp2.model;

/*
    Indica el Rol de un cliente dentro de su grupo familiar
     */
public enum TipoCliente{
    /*
    Cliente Principal, titular del grupo familiar
     */
    TITULAR,
    /*
    Cliente vinculado al titular (conyuge o hijo)
     */
    ADHERENTE
}
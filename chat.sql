-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Versione server:              10.4.32-MariaDB - mariadb.org binary distribution
-- S.O. server:                  Win64
-- HeidiSQL Versione:            12.8.0.6908
-- --------------------------------------------------------
-- Dump della struttura del database chatmultiutente
CREATE DATABASE IF NOT EXISTS `chatmultiutente` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */;
USE `chatmultiutente`;


CREATE TABLE IF NOT EXISTS `gruppi` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nome` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE IF NOT EXISTS `messaggi` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mittente` int(11) NOT NULL,
  `destinatario` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `contenuto` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `mittente` (`mittente`),
  CONSTRAINT `messaggi_ibfk_1` FOREIGN KEY (`mittente`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=629 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE IF NOT EXISTS `relazione_utenti` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_gruppo` int(11) NOT NULL,
  `id_utente` int(11) NOT NULL,
  `da_leggere` int(11) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `id_gruppo` (`id_gruppo`),
  KEY `id_utente` (`id_utente`),
  CONSTRAINT `relazione_utenti_ibfk_1` FOREIGN KEY (`id_gruppo`) REFERENCES `gruppi` (`id`) ON DELETE CASCADE,
  CONSTRAINT `relazione_utenti_ibfk_2` FOREIGN KEY (`id_utente`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=68 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE IF NOT EXISTS `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nome` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

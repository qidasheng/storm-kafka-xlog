-- phpMyAdmin SQL Dump
-- version 3.2.5
-- http://www.phpmyadmin.net
--
-- Host: 10.19.1.179:3306
-- Generation Time: Dec 02, 2014 at 05:19 PM
-- Server version: 5.1.37
-- PHP Version: 5.2.10

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `storm`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `storm_xlog` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `storm_xlog`;

-- --------------------------------------------------------

--
-- Table structure for table `ips`
--

CREATE TABLE IF NOT EXISTS `ips` (
  `id` mediumint(11) NOT NULL AUTO_INCREMENT,
  `topic` varchar(25) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `ip` varchar(255) NOT NULL,
  `time_start` bigint(10) NOT NULL,
  `time_end` bigint(10) NOT NULL,
  `total` mediumint(11) NOT NULL,
  `statics` mediumint(11) NOT NULL,
  `dynamics` mediumint(11) NOT NULL,
  `2xx` mediumint(11) NOT NULL,
  `3xx` mediumint(11) NOT NULL,
  `4xx` mediumint(11) NOT NULL,
  `5xx` mediumint(11) NOT NULL,
  `get` mediumint(11) NOT NULL DEFAULT '0',
  `post` mediumint(11) NOT NULL DEFAULT '0',
  `head` mediumint(11) NOT NULL DEFAULT '0',
  `other` mediumint(11) NOT NULL DEFAULT '0',
  `scope` mediumint(11) NOT NULL DEFAULT '0',
  `sqlxss` mediumint(11) NOT NULL DEFAULT '0',
  `useragent` mediumint(11) NOT NULL DEFAULT '0',
  `proxy` mediumint(11) NOT NULL DEFAULT '0',
  `fuck` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `time_start` (`time_start`),
  KEY `ip` (`ip`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=66130 ;

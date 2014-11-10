-- MySQL dump 10.13  Distrib 5.1.37, for unknown-linux-gnu (x86_64)
--
-- Host: localhost    Database: storm_xlog
-- ------------------------------------------------------
-- Server version       5.1.37-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `storm_xlog`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `storm_xlog` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `storm_xlog`;


--
-- Table structure for table `ips`
--

DROP TABLE IF EXISTS `ips`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ips` (
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
  PRIMARY KEY (`id`),
  KEY `time_start` (`time_start`),
  KEY `ip` (`ip`)
) ENGINE=MyISAM AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ips`
--

LOCK TABLES `ips` WRITE;
/*!40000 ALTER TABLE `ips` DISABLE KEYS */;
/*!40000 ALTER TABLE `ips` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;


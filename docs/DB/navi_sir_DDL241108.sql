-- DROP SCHEMA public CASCADE; 
-- CREATE SCHEMA public;
-- drop database navi_sir;

CREATE DATABASE navi_sir;

-- 데이터베이스가 생성된 후에 이 스크립트를 실행합니다.

CREATE TABLE parking_info (
    num_of_index SERIAL PRIMARY KEY,
    pklt_cd VARCHAR(100) UNIQUE,
    pklt_nm VARCHAR(100),
    addr VARCHAR(255),
    pklt_type VARCHAR(10),
    oper_se_nm VARCHAR(20),
    prk_stts_yn VARCHAR(10),
    tpkct INT,
    now_prk_vhcl_cnt INT,
    now_prk_vhcl_updt_tm TIMESTAMP,
    nght_pay_yn_nm CHAR(10),
    wd_oper_bgng_tm CHAR(4),
    wd_oper_end_tm CHAR(4),
    we_oper_bgng_tm CHAR(4),
    we_oper_end_tm CHAR(4),
    lhldy_oper_bgng_tm CHAR(4),
    lhldy_oper_end_tm CHAR(4),
    sat_chgd_free_se CHAR(10),
    lhldy_chgd_free_se CHAR(10),
    bsc_prk_crg INT,
    bsc_prk_hr INT,
    add_prk_crg INT,
    add_prk_hr INT,
    day_max_crg VARCHAR(10),
    lat DECIMAL(10, 7),
    lot DECIMAL(10, 7)
);

CREATE TABLE ch_pa_marker (
    num_of_index SERIAL PRIMARY KEY,
    charger_code SERIAL UNIQUE,
    parking_code VARCHAR(100) NOT NULL,
    addr VARCHAR(255),
    lat DECIMAL(10, 7),
    lng DECIMAL(10, 7),
    FOREIGN KEY (parking_code) REFERENCES parking_info(pklt_cd) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE charge_info (
    num_of_index SERIAL PRIMARY KEY,
    charger_code INT NOT NULL,
    charger_name VARCHAR(100),
    addr VARCHAR(255),
    operation_rule_nm VARCHAR(100),
    pay_yn CHAR(1),
    rates DECIMAL(10, 2),
    add_rates DECIMAL(10, 2),
    day_maximum DECIMAL(10, 2),
    lat DECIMAL(10, 7),
    lng DECIMAL(10, 7),
    charger_type VARCHAR(10),
    FOREIGN KEY (charger_code) REFERENCES ch_pa_marker(charger_code) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE user_info (
    user_code SERIAL PRIMARY KEY,
    name VARCHAR(10),
    id VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL,
    birthday DATE,
    sex CHAR(1),
    user_tel CHAR(11),
    user_addr VARCHAR(255),
    disabled_human CHAR(1),
    multiple_child CHAR(1),
    electric_car CHAR(1),
    person_merit CHAR(1),
    tax_payment CHAR(1),
    alone_family CHAR(1)
);

CREATE TABLE user_custom_info (
    user_code INT PRIMARY KEY,
    area_1 VARCHAR(255),
    area_1_alias VARCHAR(20),
    area_2 VARCHAR(255),
    area_2_alias VARCHAR(20),
    FOREIGN KEY (user_code) REFERENCES user_info(user_code) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE user_history (
    user_code INT,
    departure VARCHAR(255),
    destination VARCHAR(255),
    kilometers DECIMAL(10, 2),
    date DATE,
    FOREIGN KEY (user_code) REFERENCES user_info(user_code) ON DELETE CASCADE ON UPDATE CASCADE
);
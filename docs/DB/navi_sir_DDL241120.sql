-- DROP SCHEMA public CASCADE; 
-- CREATE SCHEMA public;
-- drop database navi_sir;

CREATE DATABASE navi_sir;

-- 데이터베이스가 생성된 후에 이 스크립트를 실행합니다.

-- public.parking_info definition

-- Drop table

-- DROP TABLE public.parking_info;

CREATE TABLE public.parking_info (
	pklt_cd varchar(100) NULL, -- 주차장코드
	pklt_nm varchar(100) NULL, -- 주차장명
	addr varchar(255) NULL, -- 주소
	oper_se_nm varchar(20) NULL, -- 운영구분
	prk_stts_yn varchar(10) NULL, -- 주차현황 정보 제공여부
	tpkct int4 NULL, -- 총 주차면
	now_prk_vhcl_cnt int4 NULL, -- 현재 주차 차량수
	now_prk_vhcl_updt_tm timestamp NULL, -- 현재 주차 차량수 업데이트시간
	nght_pay_yn_nm bpchar(10) NULL, -- 야간무료개방여부명
	wd_oper_bgng_tm bpchar(4) NULL, -- 평일 운영 시작시각(HHMM)
	wd_oper_end_tm bpchar(4) NULL, -- 평일 운영 종료시각(HHMM)
	we_oper_bgng_tm bpchar(4) NULL, -- 주말 운영 시작시각(HHMM)
	we_oper_end_tm bpchar(4) NULL, -- 주말 운영 종료시각(HHMM)
	lhldy_oper_bgng_tm bpchar(4) NULL, -- 공휴일 운영 시작시각(HHMM)
	lhldy_oper_end_tm bpchar(4) NULL, -- 공휴일 운영 종료시각(HHMM)
	sat_chgd_free_se bpchar(10) NULL, -- 토요일 유,무료 구분
	lhldy_chgd_free_se bpchar(10) NULL, -- 공휴일 유,무료 구분
	bsc_prk_crg int4 NULL, -- 기본 주차 요금
	bsc_prk_hr int4 NULL, -- 기본 주차 시간(분 단위)
	add_prk_crg int4 NULL, -- 추가 단위 요금
	add_prk_hr int4 NULL, -- 추가 단위 시간(분 단위)
	day_max_crg varchar(10) NULL, -- 일일최대요금
	lat numeric(10, 7) NULL, -- 주차장 위치 좌표 위도
	lot numeric(10, 7) NULL, -- 주차장 위치 좌표 경도
	CONSTRAINT parking_info_pklt_cd_key UNIQUE (pklt_cd)
);

-- Column comments

COMMENT ON COLUMN public.parking_info.pklt_cd IS '주차장코드';
COMMENT ON COLUMN public.parking_info.pklt_nm IS '주차장명';
COMMENT ON COLUMN public.parking_info.addr IS '주소';
COMMENT ON COLUMN public.parking_info.oper_se_nm IS '운영구분';
COMMENT ON COLUMN public.parking_info.prk_stts_yn IS '주차현황 정보 제공여부';
COMMENT ON COLUMN public.parking_info.tpkct IS '총 주차면';
COMMENT ON COLUMN public.parking_info.now_prk_vhcl_cnt IS '현재 주차 차량수';
COMMENT ON COLUMN public.parking_info.now_prk_vhcl_updt_tm IS '현재 주차 차량수 업데이트시간';
COMMENT ON COLUMN public.parking_info.nght_pay_yn_nm IS '야간무료개방여부명';
COMMENT ON COLUMN public.parking_info.wd_oper_bgng_tm IS '평일 운영 시작시각(HHMM)';
COMMENT ON COLUMN public.parking_info.wd_oper_end_tm IS '평일 운영 종료시각(HHMM)';
COMMENT ON COLUMN public.parking_info.we_oper_bgng_tm IS '주말 운영 시작시각(HHMM)';
COMMENT ON COLUMN public.parking_info.we_oper_end_tm IS '주말 운영 종료시각(HHMM)';
COMMENT ON COLUMN public.parking_info.lhldy_oper_bgng_tm IS '공휴일 운영 시작시각(HHMM)';
COMMENT ON COLUMN public.parking_info.lhldy_oper_end_tm IS '공휴일 운영 종료시각(HHMM)';
COMMENT ON COLUMN public.parking_info.sat_chgd_free_se IS '토요일 유,무료 구분';
COMMENT ON COLUMN public.parking_info.lhldy_chgd_free_se IS '공휴일 유,무료 구분';
COMMENT ON COLUMN public.parking_info.bsc_prk_crg IS '기본 주차 요금';
COMMENT ON COLUMN public.parking_info.bsc_prk_hr IS '기본 주차 시간(분 단위)';
COMMENT ON COLUMN public.parking_info.add_prk_crg IS '추가 단위 요금';
COMMENT ON COLUMN public.parking_info.add_prk_hr IS '추가 단위 시간(분 단위)';
COMMENT ON COLUMN public.parking_info.day_max_crg IS '일일최대요금';
COMMENT ON COLUMN public.parking_info.lat IS '주차장 위치 좌표 위도';
COMMENT ON COLUMN public.parking_info.lot IS '주차장 위치 좌표 경도';

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
    alone_family CHAR(1),
    ues_yn CHAR(1)
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

create table scheduled_info (
	collect_data_shce int,
	delect_data_shce int,
	sort_data_shce int,
	tran_model_shce int
	);
	
create table model_list(
	num_of_index SERIAL PRIMARY key,
	model_name varchar(100),
	model_path varchar(100),
	model_acc varchar(100),
	model_comment varchar(100),
	model_use_YN varchar(1) DEFAULT 'N'
	);
CREATE TABLE IF NOT EXISTS mock_app
(
    id          INT ( 10 ) AUTO_INCREMENT NOT NULL,
    app_env     VARCHAR(128) DEFAULT '' NOT NULL,
    app_name    VARCHAR(128) DEFAULT '' NOT NULL,
    ip          VARCHAR(128) DEFAULT '' NOT NULL,
    `port`      INT ( 5 ) DEFAULT 0 NOT NULL,
    version     VARCHAR(128) DEFAULT '' NOT NULL,
    is_enable   TINYINT ( 1 ) DEFAULT 1 NOT NULL,
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS mock_config
(
    id                   INT ( 10 ) AUTO_INCREMENT NOT NULL,
    app_env              VARCHAR(128)  DEFAULT '' NOT NULL,
    app_name             VARCHAR(128)  DEFAULT '' NOT NULL,
    mock_class           VARCHAR(256)  DEFAULT '' NOT NULL,
    mock_method          VARCHAR(128)  DEFAULT '' NOT NULL,
    parameter_rules      VARCHAR(1024) DEFAULT '' NOT NULL,
    return_or_throw_data VARCHAR(2048) DEFAULT '' NOT NULL,
    is_enable            TINYINT ( 1 ) DEFAULT 1 NOT NULL,
    create_time          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    update_time          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
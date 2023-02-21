CREATE TABLE public.ud_user_claims
(
    user_id           VARCHAR(255) NOT NULL,
    claims            TEXT         NOT NULL,
    timestamp_created TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    timestamp_updated TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT ud_user_claims_pkey PRIMARY KEY (user_id)
);

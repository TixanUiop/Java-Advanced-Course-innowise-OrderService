-- changeset evgeny:002
CREATE TYPE order_status AS ENUM ('Awaiting', 'Accepted', 'Collect', 'Sent', 'ReadyToReceive');

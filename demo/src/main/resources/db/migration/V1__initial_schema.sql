-- Initial database schema for TrimminFlow
-- Creates all tables with proper relationships and indexes

-- Create tables in order (dependencies first)

CREATE TABLE barbershop (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(50),
    address TEXT,
    description TEXT,
    timezone VARCHAR(50) DEFAULT 'UTC',
    business_hours JSONB,
    qr_code_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    barbershop_id UUID NOT NULL REFERENCES barbershop(id) ON DELETE CASCADE,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'OWNER',
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    last_login TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE barber (
    id UUID PRIMARY KEY,
    barbershop_id UUID NOT NULL REFERENCES barbershop(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    bio TEXT,
    qr_code_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    working_hours JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE service (
    id UUID PRIMARY KEY,
    barbershop_id UUID NOT NULL REFERENCES barbershop(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL,
    duration_minutes INTEGER NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE customer (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE appointment (
    id UUID PRIMARY KEY,
    barbershop_id UUID NOT NULL REFERENCES barbershop(id) ON DELETE CASCADE,
    barber_id UUID NOT NULL REFERENCES barber(id) ON DELETE CASCADE,
    customer_id UUID NOT NULL REFERENCES customer(id) ON DELETE CASCADE,
    service_id UUID NOT NULL REFERENCES service(id) ON DELETE CASCADE,
    appointment_date DATE NOT NULL,
    start_time TIME WITHOUT TIME ZONE NOT NULL,
    end_time TIME WITHOUT TIME ZONE NOT NULL,
    status VARCHAR(50) DEFAULT 'CONFIRMED',
    price NUMERIC(10,2),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE payment (
    id UUID PRIMARY KEY,
    appointment_id UUID NOT NULL REFERENCES appointment(id) ON DELETE CASCADE,
    stripe_payment_id VARCHAR(255),
    amount NUMERIC(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'EUR',
    status VARCHAR(50) DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create indexes for better query performance
CREATE INDEX idx_users_barbershop_id ON users(barbershop_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_barber_barbershop_id ON barber(barbershop_id);
CREATE INDEX idx_service_barbershop_id ON service(barbershop_id);
CREATE INDEX idx_appointment_barbershop_id ON appointment(barbershop_id);
CREATE INDEX idx_appointment_barber_id ON appointment(barber_id);
CREATE INDEX idx_appointment_customer_id ON appointment(customer_id);
CREATE INDEX idx_appointment_date ON appointment(appointment_date);
CREATE INDEX idx_customer_phone ON customer(phone);
CREATE INDEX idx_payment_appointment_id ON payment(appointment_id);

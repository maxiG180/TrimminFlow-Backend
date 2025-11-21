-- Create appointments table
CREATE TABLE appointments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    barbershop_id UUID NOT NULL,
    barber_id UUID NOT NULL,
    service_id UUID NOT NULL,
    
    appointment_date_time TIMESTAMP NOT NULL,
    end_date_time TIMESTAMP NOT NULL,
    
    customer_name VARCHAR(100) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(20),
    
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign keys
    CONSTRAINT fk_appointment_barbershop FOREIGN KEY (barbershop_id) REFERENCES barbershops(id) ON DELETE CASCADE,
    CONSTRAINT fk_appointment_barber FOREIGN KEY (barber_id) REFERENCES barbers(id) ON DELETE CASCADE,
    CONSTRAINT fk_appointment_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE RESTRICT,
    
    -- Constraints
    CONSTRAINT chk_appointment_time CHECK (appointment_date_time < end_date_time),
    CONSTRAINT chk_appointment_status CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW'))
);

-- Indexes for performance
CREATE INDEX idx_appointments_barbershop ON appointments(barbershop_id);
CREATE INDEX idx_appointments_barber ON appointments(barber_id);
CREATE INDEX idx_appointments_datetime ON appointments(appointment_date_time);
CREATE INDEX idx_appointments_status ON appointments(status);
CREATE INDEX idx_appointments_barber_datetime ON appointments(barber_id, appointment_date_time);

-- Comments
COMMENT ON TABLE appointments IS 'Stores customer appointments/bookings';
COMMENT ON COLUMN appointments.appointment_date_time IS 'Start time of the appointment';
COMMENT ON COLUMN appointments.end_date_time IS 'End time calculated from start + service duration';
COMMENT ON COLUMN appointments.status IS 'Current status: PENDING, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW';

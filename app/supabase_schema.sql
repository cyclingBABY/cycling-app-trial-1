-- ============================================================================
-- SUPABASE POSTGRESQL DATABASE SCHEMA
-- Application: Cycling Workout Companion (CWC)
-- Created for: Users, Profiles, Rides, Posts, and Clubs Tracking
-- ============================================================================

-- Enable PostGIS extension for spatial queries (optional but highly recommended for routing/geofencing)
CREATE EXTENSION IF NOT EXISTS postgis;

-- Clean Up existing tables if re-deploying (ordered due to foreign key constraints)
DROP TABLE IF EXISTS posts CASCADE;
DROP TABLE IF EXISTS club_memberships CASCADE;
DROP TABLE IF EXISTS clubs CASCADE;
DROP TABLE IF EXISTS rides CASCADE;
DROP TABLE IF EXISTS profiles CASCADE;

-- ============================================================================
-- 1. PROFILES TABLE (Linked with Supabase auth.users)
-- ============================================================================
CREATE TABLE profiles (
    id UUID REFERENCES auth.users(id) ON DELETE CASCADE PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    full_name VARCHAR(100),
    avatar_url TEXT,
    bio TEXT,
    hardware_specs JSONB DEFAULT '{}'::jsonb, -- e.g., {"bike": "Gravel X", "gps_device": "Garmin Edge"}
    emergency_contact_phone VARCHAR(30),
    is_admin BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexing for fast username searches
CREATE INDEX idx_profiles_username ON profiles(username);

-- ============================================================================
-- 2. RIDES TABLE (Tracking Ride Data & Route Telemetry)
-- ============================================================================
CREATE TABLE rides (
    ride_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES profiles(id) ON DELETE CASCADE NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    distance NUMERIC(10, 2) NOT NULL, -- Distance in kilometers or meters
    duration INTEGER NOT NULL, -- Duration in seconds
    speeds NUMERIC(5, 2)[] DEFAULT '{}', -- Array of speed samples during the ride
    average_speed NUMERIC(5, 2) GENERATED ALWAYS AS (
        CASE 
            WHEN duration > 0 THEN (distance / (duration / 3600.0))
            ELSE 0.00
        END
    ) STORED,
    elevation_gain INTEGER DEFAULT 0, -- Elevation gain in meters
    route_data JSONB NOT NULL DEFAULT '[]'::jsonb, -- Array of coordinate nodes: [{"lat": 0.32, "lng": 32.58, "t": 171891}, ...]
    geom GEOMETRY(LineString, 4326), -- PostGIS Spatial geometry path column
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexing for efficient query performance
CREATE INDEX idx_rides_user_id ON rides(user_id);
CREATE INDEX idx_rides_start_time ON rides(start_time DESC);
CREATE INDEX idx_rides_spatial ON rides USING GIST(geom); -- Spatial index for map distance lookup

-- Automatically convert route_data JSONB coordinates to a PostGIS LineString geometry on INSERT or UPDATE
CREATE OR REPLACE FUNCTION update_ride_geometry()
RETURNS TRIGGER AS $$
DECLARE
    points_coords TEXT;
BEGIN
    -- Check if route_data contains elements
    IF jsonb_array_length(NEW.route_data) > 1 THEN
        SELECT string_agg(concat(elem->>'lng', ' ', elem->>'lat'), ', ')
        INTO points_coords
        FROM jsonb_array_elements(NEW.route_data) AS elem;
        
        NEW.geom := ST_GeomFromText('LINESTRING(' || points_coords || ')', 4326);
    END IF;
    RETURN NEW;
EXCEPTION
    WHEN OTHERS THEN
        -- Catch layout errors, proceed without crashing
        RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_ride_geometry
BEFORE INSERT OR UPDATE ON rides
FOR EACH ROW
EXECUTE FUNCTION update_ride_geometry();

-- ============================================================================
-- 3. CLUBS TABLE (Rider Registries and Communities)
-- ============================================================================
CREATE TABLE clubs (
    club_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    logo_url TEXT,
    is_verified BOOLEAN DEFAULT FALSE,
    banner_image TEXT,
    city VARCHAR(100) DEFAULT 'Kampala',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_clubs_name ON clubs(name);

-- ============================================================================
-- 4. CLUB MEMBERSHIPS TABLE (ManyToMany relation profiles <-> clubs)
-- ============================================================================
CREATE TABLE club_memberships (
    membership_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    club_id UUID REFERENCES clubs(club_id) ON DELETE CASCADE NOT NULL,
    user_id UUID REFERENCES profiles(id) ON DELETE CASCADE NOT NULL,
    role VARCHAR(30) DEFAULT 'member', -- member, moderator, admin
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(club_id, user_id)
);

CREATE INDEX idx_club_memberships_user ON club_memberships(user_id);

-- ============================================================================
-- 5. POSTS TABLE (Social Dashboard and Feeds)
-- ============================================================================
CREATE TABLE posts (
    post_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES profiles(id) ON DELETE CASCADE NOT NULL,
    caption TEXT NOT NULL,
    category VARCHAR(50) DEFAULT 'General', -- Announcement, Route, Achievement, General
    post_type VARCHAR(20) DEFAULT 'text', -- text, video, photo, ride
    image_url TEXT, -- Primary photo seed or url
    video_url TEXT, -- Video path or source 
    video_duration_sec INTEGER,
    is_photo_gallery BOOLEAN DEFAULT FALSE,
    gallery_seeds TEXT[], -- Array of photo image indicators
    linked_ride_id UUID REFERENCES rides(ride_id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);

-- ============================================================================
-- 6. ROW-LEVEL SECURITY (RLS) POLICIES FOR SUPABASE
-- ============================================================================

-- Activate Row Level Security for all tables
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE rides ENABLE ROW LEVEL SECURITY;
ALTER TABLE clubs ENABLE ROW LEVEL SECURITY;
ALTER TABLE club_memberships ENABLE ROW LEVEL SECURITY;
ALTER TABLE posts ENABLE ROW LEVEL SECURITY;

-- Profiles Policies
CREATE POLICY "Public profiles are viewable by everyone" ON profiles
    FOR SELECT USING (true);

CREATE POLICY "Users can update their own profile" ON profiles
    FOR UPDATE USING (auth.uid() = id);

-- Rides Policies
CREATE POLICY "Rides are viewable by everyone" ON rides
    FOR SELECT USING (true);

CREATE POLICY "Users can create their own rides" ON rides
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own rides" ON rides
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can delete their own rides" ON rides
    FOR DELETE USING (auth.uid() = user_id);

-- Clubs Policies
CREATE POLICY "Clubs are viewable by registered users" ON clubs
    FOR SELECT USING (true);

CREATE POLICY "Only authorized administrators can manage clubs" ON clubs
    FOR ALL USING (
        EXISTS (
            SELECT 1 FROM profiles 
            WHERE profiles.id = auth.uid() AND profiles.is_admin = TRUE
        )
    );

-- Club Memberships Policies
CREATE POLICY "Memberships are visible to all users" ON club_memberships
    FOR SELECT USING (true);

CREATE POLICY "Users can join clubs themselves" ON club_memberships
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can leave clubs themselves" ON club_memberships
    FOR DELETE USING (auth.uid() = user_id);

-- Posts Policies
CREATE POLICY "Posts are viewable by everyone" ON posts
    FOR SELECT USING (true);

CREATE POLICY "Users can create posts" ON posts
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can modify/delete their own posts" ON posts
    FOR ALL USING (auth.uid() = user_id);

-- ============================================================================
-- 7. AUTOMATION FUNCTION: Automatic Profile generation from auth.users signup
-- ============================================================================
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.profiles (id, username, full_name, avatar_url, bio, is_admin)
  VALUES (
    new.id,
    COALESCE(new.raw_user_meta_data->>'username', split_part(new.email, '@', 1)),
    COALESCE(new.raw_user_meta_data->>'full_name', ''),
    new.raw_user_meta_data->>'avatar_url',
    'Avid Ugandan Cyclist',
    COALESCE((new.email = 'stuartdonsms@gmail.com'), false)
  );
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger to execute upon sign-up validation
CREATE OR REPLACE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

from sqlalchemy import Column, String, Float, DateTime, ForeignKey, JSON
from sqlalchemy.sql import func
from src.core.database import Base

class UserORM(Base):
    __tablename__ = "user_profiles"

    id = Column(String, primary_key=True, index=True)
    email = Column(String, unique=True, index=True, nullable=False)
    name = Column(String, nullable=False)
    stripe_customer_id = Column(String, nullable=True)
    dietary_flags = Column(JSON, default=list)
    allergies = Column(JSON, default=list)
    brand_preferences = Column(JSON, default=dict)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

class CartSessionORM(Base):
    __tablename__ = "cart_sessions"

    id = Column(String, primary_key=True, index=True)
    user_id = Column(String, ForeignKey("user_profiles.id"), nullable=False, index=True)
    status = Column(String, default="active")  # active, completed, abandoned
    items = Column(JSON, default=list)
    cart_total = Column(Float, default=0.0)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())

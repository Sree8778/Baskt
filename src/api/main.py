import os
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse
from src.core.redis import close_redis_client
from src.api.routers import voice, cart, checkout, catalog, p2p

STATIC_INDEX_PATH = os.path.join(os.path.dirname(__file__), "..", "static", "index.html")

@asynccontextmanager
async def lifespan(app: FastAPI):
    yield
    await close_redis_client()

app = FastAPI(
    title="US Voice Grocery Assistant API",
    version="1.0.0",
    description="Backend gateway for LiveKit WebRTC tokens, real-time cart synchronization, and checkout.",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(voice.router, prefix="/api/v1")
app.include_router(cart.router, prefix="/api/v1")
app.include_router(checkout.router, prefix="/api/v1")
app.include_router(catalog.router, prefix="/api/v1")
app.include_router(p2p.router, prefix="/api/v1")

@app.get("/")
async def root():
    if os.path.exists(STATIC_INDEX_PATH):
        return FileResponse(STATIC_INDEX_PATH)
    return {"message": "Voice Grocery Assistant API is active"}

@app.get("/health")
async def health_check():
    return {"status": "ok", "service": "grocery-voice-assistant"}

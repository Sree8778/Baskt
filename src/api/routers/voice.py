import json
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from src.core.config import settings

router = APIRouter(prefix="/voice", tags=["Voice WebRTC"])

class TokenRequest(BaseModel):
    user_id: str
    room_name: str
    preferences: dict = {}

class TokenResponse(BaseModel):
    token: str
    livekit_url: str
    room_name: str

@router.post("/token", response_model=TokenResponse)
async def generate_voice_token(request: TokenRequest):
    """Generates a LiveKit WebRTC Access Token with metadata and data channel permissions."""
    if not settings.livekit_api_key or not settings.livekit_api_secret:
        raise HTTPException(status_code=500, detail="LiveKit credentials are not configured")

    metadata = json.dumps({
        "user_id": request.user_id,
        "preferences": request.preferences,
    })

    try:
        from livekit import api
        token = api.AccessToken(settings.livekit_api_key, settings.livekit_api_secret) \
            .with_identity(request.user_id) \
            .with_name(f"User-{request.user_id[:4]}") \
            .with_metadata(metadata) \
            .with_grants(api.VideoGrants(
                room_join=True,
                room=request.room_name,
                can_publish=True,
                can_subscribe=True,
                can_publish_data=True,
            ))
        jwt_token = token.to_jwt()
    except ImportError:
        # Fallback simulation if livekit library is not yet installed
        jwt_token = f"simulated_token_{request.user_id}_{request.room_name}"

    return TokenResponse(token=jwt_token, livekit_url=settings.livekit_url, room_name=request.room_name)

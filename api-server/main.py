from contextlib import asynccontextmanager

import uvicorn
from fastapi import FastAPI

from api import router as api_router
from core.config import settings
from core.models.db_helper import db_helper

@asynccontextmanager
async def lifespan(app: FastAPI):
    yield
    await db_helper.dispose()
main_app = FastAPI(
    lifespan=lifespan
)
main_app.include_router(api_router, prefix=settings.api.prefix)
@main_app.get("/")
async def root():
    return {"message": "Hello World"}

@main_app.get("/")
def hello_index():
    return {
        "message": "Hello index!",
    }

@main_app.get("/hello/")
def hello(name: str = "World"):
    name = name.strip().title()
    return {"message": f"Hello {name}!"}

@main_app.get("/calc/add/")
def add(a: int, b: int):
    return {
        "a": a,
        "b": b,
        "result": a + b,
    }

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000, reload=True)
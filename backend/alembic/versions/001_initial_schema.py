"""initial schema for packs, corrections and telemetry

Revision ID: 001_initial
Revises: 
Create Date: 2026-09-29 10:45:00.000000

"""
from typing import Sequence, Union
from alembic import op
import sqlalchemy as sa

revision: str = '001_initial'
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table(
        'language_packs',
        sa.Column('pack_id', sa.String(length=64), nullable=False),
        sa.Column('language_code', sa.String(length=16), nullable=False),
        sa.Column('language_name', sa.String(length=64), nullable=False),
        sa.Column('native_name', sa.String(length=64), nullable=False),
        sa.Column('primary_script', sa.String(length=32), nullable=False),
        sa.Column('latest_version', sa.String(length=32), nullable=False),
        sa.Column('is_active', sa.Boolean(), nullable=True),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.Column('updated_at', sa.DateTime(), nullable=True),
        sa.PrimaryKeyConstraint('pack_id')
    )
    op.create_index(op.f('ix_language_packs_language_code'), 'language_packs', ['language_code'], unique=False)
    op.create_index(op.f('ix_language_packs_pack_id'), 'language_packs', ['pack_id'], unique=False)

    op.create_table(
        'pack_versions',
        sa.Column('id', sa.Integer(), autoincrement=True, nullable=False),
        sa.Column('pack_id', sa.String(length=64), nullable=False),
        sa.Column('version', sa.String(length=32), nullable=False),
        sa.Column('min_app_version', sa.String(length=32), nullable=True),
        sa.Column('file_path', sa.String(length=256), nullable=False),
        sa.Column('file_size_bytes', sa.Integer(), nullable=False),
        sa.Column('sha256_checksum', sa.String(length=64), nullable=False),
        sa.Column('manifest_json', sa.Text(), nullable=False),
        sa.Column('phrases_count', sa.Integer(), nullable=True),
        sa.Column('fln_vocab_count', sa.Integer(), nullable=True),
        sa.Column('worksheets_count', sa.Integer(), nullable=True),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.ForeignKeyConstraint(['pack_id'], ['language_packs.pack_id'], ),
        sa.PrimaryKeyConstraint('id')
    )

    op.create_table(
        'teacher_corrections',
        sa.Column('id', sa.Integer(), autoincrement=True, nullable=False),
        sa.Column('device_id', sa.String(length=64), nullable=False),
        sa.Column('language_code', sa.String(length=16), nullable=False),
        sa.Column('hindi_input', sa.Text(), nullable=False),
        sa.Column('machine_output_native', sa.Text(), nullable=True),
        sa.Column('teacher_suggested_native', sa.Text(), nullable=False),
        sa.Column('teacher_suggested_latin', sa.Text(), nullable=True),
        sa.Column('provenance_state', sa.String(length=32), nullable=False),
        sa.Column('notes', sa.Text(), nullable=True),
        sa.Column('status', sa.String(length=32), nullable=True),
        sa.Column('confidence_score', sa.Float(), nullable=True),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.PrimaryKeyConstraint('id')
    )

    op.create_table(
        'performance_telemetry',
        sa.Column('id', sa.Integer(), autoincrement=True, nullable=False),
        sa.Column('device_id', sa.String(length=64), nullable=False),
        sa.Column('app_version', sa.String(length=32), nullable=False),
        sa.Column('android_version', sa.Integer(), nullable=False),
        sa.Column('total_ram_mb', sa.Integer(), nullable=False),
        sa.Column('peak_ram_mb', sa.Integer(), nullable=False),
        sa.Column('pipeline_mode', sa.String(length=32), nullable=False),
        sa.Column('asr_latency_ms', sa.Float(), nullable=False),
        sa.Column('match_latency_ms', sa.Float(), nullable=False),
        sa.Column('audio_latency_ms', sa.Float(), nullable=False),
        sa.Column('total_latency_ms', sa.Float(), nullable=False),
        sa.Column('cold_start', sa.Integer(), nullable=True),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.PrimaryKeyConstraint('id')
    )


def downgrade() -> None:
    op.drop_table('performance_telemetry')
    op.drop_table('teacher_corrections')
    op.drop_table('pack_versions')
    op.drop_table('language_packs')

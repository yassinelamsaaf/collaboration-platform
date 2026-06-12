export interface TeamResponse {
  id: string;
  name: string;
  description?: string;
  slug: string;
  projectId: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTeamRequest {
  name: string;
  description?: string;
}

export interface TeamMemberResponse {
  id: string;
  teamId: string;
  userId: string;
  role: 'LEADER' | 'MEMBER';
  memberName?: string;
  memberEmail?: string;
  joinedAt: string;
}

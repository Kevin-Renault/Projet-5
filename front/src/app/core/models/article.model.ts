export interface Article {
    id: number;
    title: string;
    content: string;
    createdAt: string;
    updatedAt?: string;
    authorId: number;
    topicId: number;
}

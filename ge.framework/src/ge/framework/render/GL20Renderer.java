package ge.framework.render;
import ge.framework.buffer.FloatBuffer;
import ge.framework.buffer.ShortBuffer;
import ge.framework.mesh.Mesh;
import ge.framework.profile.Profiler;
import ge.framework.shader.GL20Program;
import ge.framework.util.Matrix;
import ge.framework.util.Ray;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/**
 * Represents a renderer for OpenGL ES 2.0.
 */
public class GL20Renderer extends Renderer
{
	// Display mode
	private DisplayMode displayMode;

	// Shader program for opaque meshes
	private GL20Program opaqueProgram;

	// Shader program for transparent meshes
	private GL20Program transparentProgram;

	// Vertex buffer
	private java.nio.FloatBuffer sharedVertexBuffer;

	// Index buffer
	private java.nio.ShortBuffer sharedIndexBuffer;

	//TODO
	private Matrix4f projectionMatrix;

	//TODO
	private Matrix4f modelViewMatrix;

	//TODO
	private Matrix4f orthogonalMatrix;

	//TODO
	private Matrix4f mvpMatrix;

	//TODO
	private java.nio.FloatBuffer mvpMatrixBuffer;

	// Profiler
	public Profiler profiler;

	//TODO
	private int visBatchCount;

	/**
	 * Constructor.
	 */
	public GL20Renderer()
	{
		// Call super constructor
		super();

		// Create shared vertex buffer
		//TODO
		sharedVertexBuffer = BufferUtils.createFloatBuffer(16 * 16 * 16 * 6 * 4 * 12);

		// Create shared index buffer
		//TODO
		sharedIndexBuffer = BufferUtils.createShortBuffer(16 * 16 * 16 * 6 * 4);

		//TODO
		projectionMatrix = new Matrix4f();
		modelViewMatrix = new Matrix4f();
		orthogonalMatrix = new Matrix4f();
		mvpMatrix = new Matrix4f();

		//TODO
		mvpMatrixBuffer = BufferUtils.createFloatBuffer(16);

		// Create profiler
		profiler = new Profiler();
	}

	//TODO
	public void setOpaqueProgram(
		final GL20Program opaqueProgram)
	{
		this.opaqueProgram = opaqueProgram;
	}

	//TODO
	public void setTransparentProgram(
		final GL20Program transparentProgram)
	{
		this.transparentProgram = transparentProgram;
	}

	/**
	 * Create display.
	 */
	public void createDisplay() throws java.lang.Exception
	{
		// Control mouse
		Mouse.setGrabbed(true);

		// Set display mode
		displayMode = Display.getDesktopDisplayMode();
//		displayMode = new DisplayMode(1280, 720);
//		displayMode = new DisplayMode(800, 450);
		Display.setDisplayMode(displayMode);

		//TODO
		Display.setInitialBackground(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue());

		//TODO
		Display.setVSyncEnabled(true);
		Display.setFullscreen(true);

		// Create display
		Display.create();

		//TODO
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
//		GL11.glDepthMask(true);
//		GL11.glShadeModel(GL11.GL_SMOOTH);
	}

	/**
	 * Set projection from camera parameters.
	 */
	private void setProjection()
	{
		// Reset projection matrix
		Matrix.glMatrixMode(Matrix.GL_PROJECTION);
		Matrix.glLoadIdentity();

		// Set viewing perspective
		Matrix.gluPerspective(camera.getFieldOfView(), ((float) displayMode.getWidth() / (float) displayMode.getHeight()), 0.1f, camera.getViewingDistance());

		// Get projection matrix
		Matrix.glMatrixMode(Matrix.GL_PROJECTION);
		Matrix.glGetMatrix(projectionMatrix);
	}

	/**
	 * Prepare mesh for rendering.
	 * @param mesh The mesh
	 */
	protected void prepareMesh(
		final Mesh mesh)
	{
		// Local variables
		FloatBuffer vertexBuffer;
		ShortBuffer indexBuffer;
		int vertexBufferId;
		int indexBufferId;

		// Get vertex buffer data
		vertexBuffer = mesh.getVertexBuffer();
		sharedVertexBuffer.put(vertexBuffer.getContent(), 0, vertexBuffer.getSize());
		sharedVertexBuffer.flip();

		// Get index buffer data
		//TODO
		indexBuffer = convertIndexBuffer(mesh.getIndexBuffer());
		sharedIndexBuffer.put(indexBuffer.getContent(), 0, indexBuffer.getSize());
		sharedIndexBuffer.flip();

		// Send vertex data
		vertexBufferId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBufferId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, sharedVertexBuffer, GL15.GL_STATIC_DRAW);

		// Send index data
		indexBufferId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBufferId);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, sharedIndexBuffer, GL15.GL_STATIC_DRAW);

		// Clear buffers
		sharedVertexBuffer.clear();
		sharedIndexBuffer.clear();

		// Update mesh
		mesh.setVertexBufferId(vertexBufferId);
		mesh.setIndexBufferId(indexBufferId);
		//TODO
		mesh.setIndexCount((short)indexBuffer.getSize());
	}

	/**
	 * Destroy mesh from rendering.
	 * @param mesh The mesh
	 */
	protected void destroyMesh(
		final Mesh mesh)
	{
		// Remove vertex data
		GL15.glDeleteBuffers(mesh.getVertexBufferId());

		// Remove index data
		GL15.glDeleteBuffers(mesh.getIndexBufferId());
	}

	/**
	 * Convert index buffer from quad layout to triangle layout.
	 * @param indexBuffer The index buffer
	 * @return The converted index buffer
	 */
	private ShortBuffer convertIndexBuffer(
		final ShortBuffer indexBuffer)
	{
		// Local variables
		int size;
		short[] data;
		ShortBuffer convertedBuffer;

		// Get index buffer data
		size = indexBuffer.getSize();
		data = indexBuffer.getContent();

		// Convert index buffer data
		convertedBuffer = new ShortBuffer((int)(size * 1.5));

		for (int i = 0; i < (size / 4); i++)
		{
			int iindex = (i * 4);
			convertedBuffer.add(data[iindex + 0]);
			convertedBuffer.add(data[iindex + 1]);
			convertedBuffer.add(data[iindex + 2]);
			convertedBuffer.add(data[iindex + 2]);
			convertedBuffer.add(data[iindex + 3]);
			convertedBuffer.add(data[iindex + 0]);
		}

		return convertedBuffer;
	}

	/**
	 * Render scene.
	 */
	public boolean renderScene()
	{
		//TODO
		profiler.measure(Profiler.OTHER);

		//TODO
		// Close requested?
		if (Display.isCloseRequested() == true)
		{
			// Destroy display
			Display.destroy();

			return false;
		}

		// Has camera view changed?
		if (camera.hasViewChanged() == true)
		{
			// Set projection from camera parameters
			setProjection();
		}

		//TODO
		GL11.glClearColor(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), backgroundColor.getAlpha());

		// Clear buffers
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		//TODO
		profiler.measure(Profiler.CLEAR_BUFFER);

		// Reset model view matrix
		Matrix.glMatrixMode(Matrix.GL_MODELVIEW);
		Matrix.glLoadIdentity();

		// Rotate view to camera orientation
		Matrix.glRotatef(camera.getYaw(), 1.0f, 0.0f, 0.0f);
		Matrix.glRotatef(camera.getPitch(), 0.0f, 1.0f, 0.0f);
		//TODO
//		System.out.println((float) Math.toRadians(camera.getPitch()));

		// Move view to camera position
		Matrix.glTranslatef(camera.getPositionX(), camera.getPositionY(), camera.getPositionZ());

		//TODO
		profiler.measure(Profiler.UPDATE_MATRIX);

		// Get model view matrix
		Matrix.glMatrixMode(Matrix.GL_MODELVIEW);
		Matrix.glGetMatrix(modelViewMatrix);

		// Generate model view projection matrix
		Matrix4f.mul(projectionMatrix, modelViewMatrix, mvpMatrix);
		mvpMatrix.store(mvpMatrixBuffer);
		mvpMatrixBuffer.flip();

		//TODO
		profiler.measure(Profiler.GET_MATRIX);

		// Calculate viewing frustum for culling
		frustum.calculateFrustum(mvpMatrixBuffer);

		//TODO
		profiler.measure(Profiler.CALCULATE_FRUSTUM);

		//TODO - per mesh / per list
		// Bind texture
		if (texture != null)
		{
			texture.bind();
		}

		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
//		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
//		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
//		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
//		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
//		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST_MIPMAP_NEAREST);
//		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_NEAREST);

		//TODO
		profiler.measure(Profiler.BIND_TEXTURE);

		//TODO
		visBatchCount = 0;

		// Disable alpha blending for opaque meshes
		GL11.glDisable(GL11.GL_BLEND);
		//TODO
        // Disable lighting
//		GL11.glDisable(GL11.GL_LIGHTING);
		//TODO
        // Disable dithering
//		GL11.glDisable(GL11.GL_DITHER);

		// Activate shader program for opaque meshes
		opaqueProgram.activate();

		//TODO
		profiler.measure(Profiler.ACTIVATE_PROGRAM);

		// Set model view projection matrix in shader program
		GL20.glUniformMatrix4(opaqueProgram.getMvpMatrixUniform(), false, mvpMatrixBuffer);

		// Set texture sampler in shader program
		GL20.glUniform1i(opaqueProgram.getSamplerUniform(), 0);

		//TODO
		profiler.measure(Profiler.SET_PROGRAM_VARIABLES);

		// Render opaque meshes
		renderMeshList(opaqueMeshList,
			opaqueProgram.getPositionAttribute(), opaqueProgram.getNormalAttribute(),
			opaqueProgram.getColorAttribute(), opaqueProgram.getTextureAttribute());

		// Deactivate shader program for opaque meshes
		opaqueProgram.deactivate();

		//TODO
		profiler.measure(Profiler.DEACTIVATE_PROGRAM);

		// Scene contains transparent meshes?
		if ((transparentMeshList.size() > 0)
			|| (overlayMeshList.size() > 0))
		{
			// Enable alpha blending for transparent meshes
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); 
			GL11.glEnable(GL11.GL_BLEND);

			// Activate shader program for transparent meshes
			transparentProgram.activate();

			//TODO
			profiler.measure(Profiler.ACTIVATE_PROGRAM);

			// Set model view projection matrix in shader program
			GL20.glUniformMatrix4(transparentProgram.getMvpMatrixUniform(), false, mvpMatrixBuffer);

			// Set texture sampler in shader program
			GL20.glUniform1i(transparentProgram.getSamplerUniform(), 0);

			//TODO
			profiler.measure(Profiler.SET_PROGRAM_VARIABLES);

			// Render transparent meshes
			renderMeshList(transparentMeshList,
				transparentProgram.getPositionAttribute(), transparentProgram.getNormalAttribute(),
				transparentProgram.getColorAttribute(), transparentProgram.getTextureAttribute());

			//TODO
			if (overlayMeshList.size() > 0)
			{
				//TODO
				// Reset model view matrix
				Matrix.glMatrixMode(Matrix.GL_MODELVIEW);
				Matrix.glLoadIdentity();

				// Get model view matrix
				Matrix.glMatrixMode(Matrix.GL_MODELVIEW);
				Matrix.glGetMatrix(orthogonalMatrix);

				// Generate orthogonal projection matrix
				orthogonalMatrix.store(mvpMatrixBuffer);
				mvpMatrixBuffer.flip();

				//TODO
				profiler.measure(Profiler.GET_MATRIX);

				// Enable color invert blending for overlay meshes
//				GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ZERO);
//				GL11.glEnable(GL11.GL_BLEND);

				// Set orthogonal projection matrix in shader program
				GL20.glUniformMatrix4(transparentProgram.getMvpMatrixUniform(), false, mvpMatrixBuffer);

				//TODO
				profiler.measure(Profiler.SET_PROGRAM_VARIABLES);

				// Render overlay meshes
				renderMeshList(overlayMeshList,
					transparentProgram.getPositionAttribute(), transparentProgram.getNormalAttribute(),
					transparentProgram.getColorAttribute(), transparentProgram.getTextureAttribute());
			}

			// Deactivate shader program for transparent meshes
			transparentProgram.deactivate();

			//TODO
			profiler.measure(Profiler.DEACTIVATE_PROGRAM);
		}

		//TODO
		counters.visBatchCount = visBatchCount;

		// Swap buffers
//		GL11.glFlush();
//		GL11.glFinish();
		Display.update(false);
		Display.processMessages();

		//TODO
		profiler.measure(Profiler.SWAP_BUFFERS);

		return true;
	}

	/**
	 * Render overlays.
	 */
	public boolean renderOverlays()
	{
		//TODO
		profiler.measure(Profiler.OTHER);

		//TODO
		// Close requested?
		if (Display.isCloseRequested() == true)
		{
			// Destroy display
			Display.destroy();

			return false;
		}

		//TODO
		GL11.glClearColor(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), backgroundColor.getAlpha());

		// Clear buffers
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		//TODO
		profiler.measure(Profiler.CLEAR_BUFFER);

		//TODO
		// Reset model view matrix
		Matrix.glMatrixMode(Matrix.GL_MODELVIEW);
		Matrix.glLoadIdentity();

		// Get model view matrix
		Matrix.glMatrixMode(Matrix.GL_MODELVIEW);
		Matrix.glGetMatrix(orthogonalMatrix);

		// Generate orthogonal projection matrix
		orthogonalMatrix.store(mvpMatrixBuffer);
		mvpMatrixBuffer.flip();

		//TODO
		profiler.measure(Profiler.GET_MATRIX);

		//TODO - per mesh / per list
		// Bind texture
		if (texture != null)
		{
			texture.bind();
		}

		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);

		//TODO
		profiler.measure(Profiler.BIND_TEXTURE);

		//TODO
		visBatchCount = 0;

		// Enable alpha blending for transparent meshes
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); 
		GL11.glEnable(GL11.GL_BLEND);
		// Enable color invert blending for overlay meshes
//		GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ZERO);
//		GL11.glEnable(GL11.GL_BLEND);

		// Activate shader program for transparent meshes
		transparentProgram.activate();

		//TODO
		profiler.measure(Profiler.ACTIVATE_PROGRAM);

		// Set orthogonal projection matrix in shader program
		GL20.glUniformMatrix4(transparentProgram.getMvpMatrixUniform(), false, mvpMatrixBuffer);

		// Set texture sampler in shader program
		GL20.glUniform1i(transparentProgram.getSamplerUniform(), 0);

		//TODO
		profiler.measure(Profiler.SET_PROGRAM_VARIABLES);

		// Render overlay meshes
		renderMeshList(overlayMeshList,
			transparentProgram.getPositionAttribute(), transparentProgram.getNormalAttribute(),
			transparentProgram.getColorAttribute(), transparentProgram.getTextureAttribute());

		// Deactivate shader program for transparent meshes
		transparentProgram.deactivate();

		//TODO
		profiler.measure(Profiler.DEACTIVATE_PROGRAM);

		//TODO
		counters.visBatchCount = visBatchCount;

		// Swap buffers
//		GL11.glFlush();
//		GL11.glFinish();
		Display.update(false);
//		Display.processMessages();

		//TODO
		profiler.measure(Profiler.SWAP_BUFFERS);

		return true;
	}

	/**
	 * Render mesh list.
	 * @param meshList The mesh list
	 * @param positionAttribute The 
	 * @param normalAttribute The 
	 * @param colorAttribute The 
	 * @param textureAttribute The 
	 */
	private void renderMeshList(
		final java.util.List<Mesh> meshList,
		final int positionAttribute,
		final int normalAttribute,
		final int colorAttribute,
		final int textureAttribute)
	{
		// Local variables
		java.util.ListIterator<Mesh> iterator;
		Mesh mesh;
		boolean draw;

		//TODO - get from mesh
		int stride = 48; 
		int positionOffset = 0;
		int normalOffset = 12;
		int colorOffset = 24;
		int textureOffset = 40;

//		// Render mesh list
//		for (iterator = meshList.listIterator(); iterator.hasNext() == true;)
//		{
//			mesh = (Mesh) iterator.next();
//
//			//TODO
//			draw = false;
//
//			if (mesh.getMeshType() == Mesh.MeshType.OVERLAY)
//			{
//				draw = true;
//			}
//			else
//			{
//
//				if (frustum.boxInFrustum(mesh.getBoundingBox(), profiler) == true)
//				{
//					draw = true;
//				}
//
//			}
//
//		}
//
//		//TODO
//		profiler.measure(Profiler.BOX_IN_FRUSTUM);

		// Render mesh list
		for (iterator = meshList.listIterator(); iterator.hasNext() == true;)
		{
			mesh = (Mesh) iterator.next();

			//TODO
//			profiler.measure(Profiler.ITERATE_LOOP);

			//TODO
			draw = false;

			if (mesh.getMeshType() == Mesh.MeshType.OVERLAY)
			{
				draw = true;
			}
			else
			{

				if (frustum.boxInFrustum(mesh.getBoundingBox(), profiler) == true)
				{
					draw = true;
				}

				//TODO
//				profiler.measure(Profiler.BOX_IN_FRUSTUM);
			}

			// Bounding box within viewing frustum?
			//TODO
//			if ((mesh.getMeshType() == Mesh.MeshType.OVERLAY)
//				|| (frustum.boxInFrustum(mesh.getBoundingBox()) == true))
			if (draw == true)
			{

				//TODO
				if (mesh.getTexture() != null)
				{
					mesh.getTexture().bind();
					GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
					GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);

					//TODO
//					profiler.measure(Profiler.BIND_TEXTURE);
				}

				// Bind to vertex buffer
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.getVertexBufferId());

				//TODO
//				profiler.measure(Profiler.BIND_BUFFER);

				// Set vertex attributes
				if (positionAttribute != -1)
				{
					GL20.glVertexAttribPointer(positionAttribute, 3, GL11.GL_FLOAT, false, stride, positionOffset);
				}

				if (normalAttribute != -1)
				{
					GL20.glVertexAttribPointer(normalAttribute, 3, GL11.GL_FLOAT, false, stride, normalOffset);
				}

				if (colorAttribute != -1)
				{
					GL20.glVertexAttribPointer(colorAttribute, 4, GL11.GL_FLOAT, false, stride, colorOffset);
				}

				if (textureAttribute != -1)
				{
					GL20.glVertexAttribPointer(textureAttribute, 2, GL11.GL_FLOAT, false, stride, textureOffset);
				}

				//TODO
//				profiler.measure(Profiler.SET_ATTRIB_POINTER);

				// Bind to index buffer
				GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, mesh.getIndexBufferId());

				//TODO
//				profiler.measure(Profiler.BIND_BUFFER);

				//TODO
				if (mesh.getIndexOffsets() != null)
				{
					short[] indexOffsets = mesh.getIndexOffsets();

					//TODO
					for (int i = 0; i < indexOffsets.length; i++)
					{
						// Draw mesh
						GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getIndexCount(), GL11.GL_UNSIGNED_SHORT, indexOffsets[i]);
					}

				}
				else
				{
					// Draw mesh
					GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getIndexCount(), GL11.GL_UNSIGNED_SHORT, mesh.getIndexOffset());
				}

				//TODO
//				profiler.measure(Profiler.DRAW_ELEMENTS);

				//TODO
				visBatchCount++;
			}

		}

		//TODO
		profiler.measure(Profiler.DRAW_ELEMENTS);
	}

	//TODO
	public Ray pick(
		final float magnitude)
	{
		//TODO - picking
		Matrix4f mviMatrix = new Matrix4f();
		Matrix4f.invert(modelViewMatrix, mviMatrix);

		Vector4f camSpaceNear = new Vector4f(0, 0, 0, 1);
		Vector4f worldSpaceNear = new Vector4f();
		Matrix4f.transform(mviMatrix, camSpaceNear, worldSpaceNear);

		Vector4f camSpaceFar = new Vector4f(0, 0, magnitude, 1);
		Vector4f worldSpaceFar = new Vector4f();
		Matrix4f.transform(mviMatrix, camSpaceFar, worldSpaceFar);
		
		Vector3f rayPosition = new Vector3f(worldSpaceNear.x, worldSpaceNear.y, worldSpaceNear.z);
		Vector3f rayDirection = new Vector3f(worldSpaceFar.x - worldSpaceNear.x, worldSpaceFar.y - worldSpaceNear.y, worldSpaceFar.z - worldSpaceNear.z);
		rayDirection.normalise();
		//TODO - picking

		return new Ray(rayPosition, rayDirection);
	}

}
